package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.controller.files.BrowseFileMenuItem;
import com.larryhsiao.auxo.utils.AuxoExecute;
import com.larryhsiao.auxo.utils.ImageToFile;
import com.larryhsiao.auxo.utils.MenuIcon;
import com.larryhsiao.auxo.utils.UrlFile;
import com.larryhsiao.auxo.utils.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.views.FileListCell;
import com.larryhsiao.auxo.workspace.FsFiles;
import com.larryhsiao.juno.*;
import com.larryhsiao.clotho.Source;
import com.larryhsiao.clotho.file.FileDelete;
import com.larryhsiao.clotho.log.Log;
import com.larryhsiao.clotho.regex.IsUrl;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import okhttp3.OkHttpClient;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.sql.Connection;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.TransferMode.COPY;

/**
 * Controller of page that shows file list in Axuo.
 */
public class FileList implements Initializable {
    private final Log log;
    private final OkHttpClient client;
    private final File root;
    private final Source<Connection> db;
    private final ObservableList<File> data = observableArrayList();
    @FXML
    private TextField searchInput;
    @FXML
    private ListView<File> fileList;
    @FXML
    private AnchorPane info;

    public FileList(Log log, OkHttpClient client, File root, Source<Connection> db) {
        this.log = log;
        this.client = client;
        this.root = root;
        this.db = db;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchInput.textProperty()
            .addListener((observable, oldValue, newValue) ->
                loadFilesByKeyword(searchInput.textProperty().getValue()));
        TextFields.bindAutoCompletion(
            searchInput,
            inputView -> new QueriedTags(new AllTags(db))
                .value().values().stream()
                .filter(tag -> {
                    String input = inputView.getUserText();
                    String[] params = input.split(" ");
                    if (params.length > 1) {
                        input = params[params.length - 1];
                    }
                    return input.startsWith("#") && tag.name().startsWith(input.substring(1));
                })
                .map(tag -> {
                    String input = inputView.getUserText();
                    return input.substring(0, input.lastIndexOf("#")) + "#" + tag.name();
                })
                .collect(Collectors.toList())
        );
        loadFiles();
        fileList.setCellFactory(param -> new FileListCell(log));
        fileList.setContextMenu(new ContextMenu());
        fileList.setOnContextMenuRequested(event -> {
            fileList.getContextMenu().getItems().clear();
            fileList.getContextMenu().getItems().addAll(
                fileContextMenu(resources, new QueriedTags(new TagsByFileId(db,
                    new FileByName(db,
                        fileList.getSelectionModel().getSelectedItem()
                            .getName()).value().id()
                )).value().containsKey("favorite")).getItems());
        });
        fileList.setItems(data);
        fileList.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    return;
                }
                loadInfo(newValue, resources);
            });
        fileList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                new AuxoExecute(
                    client, log, root, ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        fileList.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                new AuxoExecute(
                    client, log, root, ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        fileList.setOnDragOver(event -> {
            event.acceptTransferModes(COPY);
            event.consume();
        });
        fileList.setOnDragDropped(event -> {
            Dragboard board = event.getDragboard();

            var textType = DataFormat.lookupMimeType("text/plain");
            if (board.hasFiles()) {
                for (File file : board.getFiles()) {
                    new Thread(() -> {
                        try {
                            moveFileIntoWorkspace(file);
                        } catch (IOException e) {
                            new ExceptionAlert(e, resources).fire();
                        }
                    }).start();
                }
                event.consume();
            } else if (board.hasImage()) {
                TextInputDialog dialog = new TextInputDialog("");
                dialog.setHeaderText(resources.getString("image_name"));
                dialog.getEditor().textProperty()
                    .addListener((observableValue, s, t1) ->
                        dialog.getDialogPane().lookupButton(ButtonType.OK)
                            .disableProperty().setValue(
                            new File(t1 + ".png").exists()
                        ));
                dialog.getEditor().setText("image");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(s -> {
                    new ImageToFile(
                        new File(s + ".png"),
                        board.getImage()
                    ).fire();
                });
            } else if (board.hasContent(textType) &&
                new IsUrl(board.getContent(textType).toString(), log).value()) {
                var url = board.getContent(textType).toString();
                new UrlFile(root, new OkHttpClient(), url).value();
                event.consume();
                return;
            }
            event.consume();
        });

        Platform.runLater(() -> {
            final Window window = fileList.getScene().getWindow();
            new Thread(() -> {
                try {
                    listenForFileChange(window, resources);
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(
                        () -> new ExceptionAlert(e, resources).fire());
                }
            }).start();
        });
    }

    private void loadFilesByKeyword(String keyword) {
        var allFiles = new QueriedAFiles(new AllFiles(db)).value();
        var dbKeywordFiles = new HashMap<String, AFile>();
        var params = Arrays.stream(keyword.split(" "))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
        if (params.size() > 0) {
            dbKeywordFiles.putAll(new QueriedAFiles(new FilesByInput(db, params.get(0))).value());
            for (String param : params.subList(1, params.size())) {
                try {
                    Map<String, AFile> value =
                        new QueriedAFiles(new FilesByInput(db, param)).value();
                    Map<String, AFile> delete = new HashMap<>();
                    dbKeywordFiles.forEach((s, aFile) -> {
                        if (!value.containsKey(s)) {
                            delete.put(s, aFile);
                        }
                    });
                    for (String key : delete.keySet()) {
                        dbKeywordFiles.remove(key);
                    }
                } catch (Exception ignore) {
                    dbKeywordFiles.clear(); // Second params can found nothing.
                }
            }
        } else {
            dbKeywordFiles.putAll(new QueriedAFiles(new FilesByInput(db, "")).value());
        }
        data.clear();
        data.addAll(
            new FsFiles(root)
                .value().entrySet().stream()
                .filter(entry -> entry.getKey().contains(keyword) ||
                    dbKeywordFiles.containsKey(entry.getKey()) ||
                    ("#!tag".equals(keyword) && !allFiles.containsKey(entry.getKey())))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (u, v) -> {
                        throw new IllegalStateException(
                            String
                                .format("Duplicate key %s", u));
                    },
                    LinkedHashMap::new)
                ).values()
        );
    }

    private ContextMenu fileContextMenu(ResourceBundle res, boolean isFav) {
        final File selected = fileList.getSelectionModel().getSelectedItem();
        final ContextMenu menu = new ContextMenu();
        final Menu createMenu = new Menu(res.getString("create"));
        createMenu.setGraphic(new MenuIcon("/images/plus.png").value());
        final MenuItem folder = new MenuItem(res.getString("folder"));
        folder.setGraphic(new MenuIcon("/images/dir.png").value());
        folder.setOnAction(event -> {
            final var dialog = new TextInputDialog();
            dialog.setContentText(res.getString("folder_name"));
            var option = dialog.showAndWait();
            option.ifPresent(s -> {
                var success = new File(root, s).mkdir();
                if (!success) {
                    var alert = new Alert(ERROR);
                    alert.setContentText(res.getString("create_file_failed"));
                    alert.show();
                }
            });
        });
        createMenu.getItems().add(folder);
        final MenuItem file = new MenuItem(res.getString("file"));
        file.setGraphic(new MenuIcon("/images/file.png").value());
        file.setOnAction(event -> {
            final var dialog = new TextInputDialog();
            dialog.setContentText(res.getString("file_name"));
            var option = dialog.showAndWait();
            option.ifPresent(s -> {
                try {
                    var success = new File(root, s).createNewFile();
                    if (!success) {
                        var alert = new Alert(ERROR);
                        alert.setContentText(
                            res.getString("create_file_failed"));
                        alert.show();
                    }
                } catch (IOException e) {
                    new ExceptionAlert(e, res).fire();
                }
            });
        });
        createMenu.getItems().add(file);
        final MenuItem nyxInstance = new MenuItem(res.getString("nyx"));
        nyxInstance.setGraphic(new MenuIcon("/images/nyx.png").value());
        nyxInstance.setOnAction(event -> {
            try {
                var format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                var nyxRoot = new File(root, format.format(new Date()));
                nyxRoot.mkdir();
                var contentFile = new File(nyxRoot, "content.txt");
                contentFile.createNewFile();
            } catch (IOException e) {
                new ExceptionAlert(e, res).fire();
            }
        });
        createMenu.getItems().add(nyxInstance);
        menu.getItems().add(createMenu);
        if (isFav) {
            final MenuItem favorite = new MenuItem(
                res.getString("remove_from_favorite"));
            favorite.setGraphic(new MenuIcon("/images/fav.png").value());
            favorite.setOnAction(event -> {
                new UnMarkFavorite(db,
                    new FileByName(db, selected.getName()).value().id()).fire();
                loadInfo(selected, res);
            });
            menu.getItems().add(favorite);
        } else {
            final MenuItem favorite = new MenuItem(res.getString("favorite"));
            favorite.setGraphic(new MenuIcon("/images/fav.png").value());
            favorite.setOnAction(event -> {
                new MarkFavorite(db,
                    new FileByName(db, selected.getName()).value().id()).fire();
                loadInfo(selected, res);
            });
            menu.getItems().add(favorite);
            loadInfo(selected, res);
        }
        final MenuItem rename = new MenuItem(res.getString("rename"));
        rename.setGraphic(new MenuIcon("/images/rename.png").value());
        rename.setOnAction(event -> {
            final TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(selected.getName());
            dialog.setHeaderText(res.getString("rename"));
            dialog.setContentText(res.getString("new_name"));
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                try {
                    File target = new File(selected.getParent(), newName);
                    renameTo(selected, target);
                    loadInfo(target, res);
                } catch (IOException e) {
                    new ExceptionAlert(e, res).fire();
                }
            });
        });
        menu.getItems().add(rename);
        final MenuItem delete = new MenuItem(res.getString("delete"));
        delete.setGraphic(new MenuIcon("/images/trash.png").value());
        delete.setOnAction(event -> {
            final Stage current = ((Stage) fileList.getScene().getWindow());

            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(selected.getName());
            alert.setContentText(MessageFormat
                .format(res.getString("delete_obj"), selected.getName()));
            alert.setHeaderText(res.getString("are_you_sure"));
            alert.setX(current.getX() + 150);
            alert.setY(current.getY() + 150);
            final Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                new FileDelete(selected).fire();
                if (selected.exists()) {
                    new ExceptionAlert(
                        new Exception("Failed to delete " + selected.getName()),
                        res
                    ).fire();
                }
            }
        });
        menu.getItems().add(delete);
        menu.getItems().add(new BrowseFileMenuItem(res, selected).value());
        if (selected.isFile()) {
            final MenuItem wrapIntoFolder = new MenuItem(
                res.getString("wrap_into_folder")
            );
            wrapIntoFolder.setGraphic(new MenuIcon("/images/box_in.png").value());
            wrapIntoFolder.setOnAction(event -> {
                try {
                    final var tempTarget = new File(root, selected.getName() + ".tmp");
                    renameTo(selected, tempTarget);
                    final var targetDir = new File(root, selected.getName());
                    targetDir.mkdir();
                    new FileRenameAction(
                        db,
                        new FileByName(db, tempTarget.getName()).value(),
                        selected.getName()
                    ).fire();
                    Files.move(tempTarget.toPath(), new File(
                        targetDir,
                        selected.getName()
                    ).toPath());
                } catch (Exception e) {
                    new ExceptionAlert(e, res).fire();
                }
            });
            menu.getItems().add(wrapIntoFolder);
        }
        return menu;
    }

    private void renameTo(File file, File target) throws IOException {
        Files.move(file.toPath(), target.toPath());
        new FileRenameAction(
            db,
            new FileByName(db, file.getName()).value(),
            target.getName()
        ).fire();
    }

    private void moveFileIntoWorkspace(File file) throws IOException {
        Files.move(file.toPath(), new File(
            root,
            file.getName()
        ).toPath());
    }

    private void loadFiles() {
        data.clear();
        data.addAll(new FsFiles(root).value().values());
    }

    private void listenForFileChange(Window window, ResourceBundle res)
        throws IOException, InterruptedException {
        try (final WatchService watchService = FileSystems.getDefault()
            .newWatchService()) {
            final WatchKey watchKey = root.toPath()
                .register(watchService, ENTRY_CREATE,
                    ENTRY_MODIFY,
                    ENTRY_DELETE);
            final AtomicBoolean running = new AtomicBoolean(true);
            window.setOnHidden(event -> running.set(false));
            while (running.get()) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    final Path changed = (Path) event.context();
                    if (changed.toFile().getAbsolutePath().contains(".auxo.")) {
                        continue;
                    }
                    Platform.runLater(() -> {
                        if (event.kind() == ENTRY_CREATE) {
                            data.add(
                                new File(root, changed.toFile().getName()));
                        } else if (event.kind() == ENTRY_DELETE) {
                            var deleted =
                                new File(root, changed.toFile().getName())
                                    .getAbsolutePath();
                            for (File file : data) {
                                if (deleted.equals(file.getAbsolutePath())) {
                                    data.remove(file);
                                    break;
                                }
                            }
                        } else if (event.kind() == ENTRY_MODIFY) {
                            loadInfo(
                                fileList.getSelectionModel().getSelectedItem(),
                                res
                            );
                        }
                    });
                }
                watchKey.reset();
                Thread.sleep(1000);
            }
        }
    }

    private void loadInfo(File selected, ResourceBundle res) {
        try {
            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/larryhsiao/auxo/file.fxml"),
                res
            );
            loader.setController(new FileInfo(log, client, root, db,
                new FileByName(db, selected.getName()).value().id()));
            info.getChildren().clear();
            info.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

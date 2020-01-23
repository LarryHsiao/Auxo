package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.AuxoExecute;
import com.larryhsiao.auxo.utils.ImageToFile;
import com.larryhsiao.auxo.utils.PlatformExecute;
import com.larryhsiao.auxo.views.FileListCell;
import com.larryhsiao.auxo.workspace.FsFiles;
import com.larryhsiao.juno.*;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.file.FileDelete;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
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
    private final File root;
    private final Source<Connection> db;
    private final ObservableList<File> data = observableArrayList();
    @FXML private TextField searchInput;
    @FXML private ListView<File> fileList;
    @FXML private AnchorPane info;

    public FileList(File root, Source<Connection> db) {
        this.root = root;
        this.db = db;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchInput.textProperty()
            .addListener((observable, oldValue, newValue) ->
                loadFilesByKeyword(searchInput.textProperty().getValue()));
        TextFields.bindAutoCompletion(searchInput,
            param -> new QueriedTags(new AllTags(db))
                .value().values().stream()
                .filter(tag -> param.getUserText().startsWith("#") &&
                    tag.name().startsWith(param.getUserText().substring(1)))
                .map(tag -> "#" + tag.name())
                .collect(Collectors.toList()));
        loadFiles();
        fileList.setCellFactory(param -> new FileListCell());
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
                    root, ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        fileList.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                new AuxoExecute(
                    root, ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        fileList.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles() ||
                event.getDragboard().hasImage()) {
                event.acceptTransferModes(COPY);
            }
            event.consume();
        });
        fileList.setOnDragDropped(event -> {
            Dragboard board = event.getDragboard();
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
            }

            if (board.hasImage()) {
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
            }
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
        data.clear();
        data.addAll(
            new FsFiles(root)
                .value().entrySet().stream()
                .filter(
                    entry -> entry.getKey().contains(keyword) ||
                        new QueriedAFiles(
                            new FilesByInput(db, keyword))
                            .value()
                            .containsKey(entry.getKey()))
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
        final MenuItem folder = new MenuItem(res.getString("folder"));
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
        menu.getItems().add(createMenu);
        if (isFav) {
            final MenuItem favorite = new MenuItem(
                res.getString("remove_from_favorite"));
            favorite.setOnAction(event -> {
                new UnMarkFavorite(db,
                    new FileByName(db, selected.getName()).value().id()).fire();
                loadInfo(selected, res);
            });
            menu.getItems().add(favorite);
        } else {
            final MenuItem favorite = new MenuItem(res.getString("favorite"));
            favorite.setOnAction(event -> {
                new MarkFavorite(db,
                    new FileByName(db, selected.getName()).value().id()).fire();
                loadInfo(selected, res);
            });
            menu.getItems().add(favorite);
            loadInfo(selected, res);
        }
        final MenuItem rename = new MenuItem(res.getString("rename"));
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
                        new Exception(
                            "Failed to delete " + selected.getName()),
                        res).fire();
                }
            }
        });
        menu.getItems().add(delete);
        final MenuItem showInBrowser = new MenuItem(
            res.getString("show_in_browser"));
        showInBrowser.setOnAction(event -> {
            File target;
            if (selected.isDirectory()) {
                target = selected;
            } else {
                target = selected.getParentFile();
            }
            new Thread(() -> new PlatformExecute(target).fire()).start();
        });
        menu.getItems().add(showInBrowser);
        if (selected.isFile()) {
            final MenuItem wrapIntoFolder = new MenuItem(
                res.getString("wrap_into_folder")
            );
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
                    if (changed.toFile().getAbsolutePath()
                        .contains(".auxo.db")) {
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
                        }else if (event.kind() == ENTRY_MODIFY){
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
            loader.setController(new FileInfo(
                    root, db,
                    new FileByName(db, selected.getName()).value().id()
                )
            );
            info.getChildren().clear();
            info.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

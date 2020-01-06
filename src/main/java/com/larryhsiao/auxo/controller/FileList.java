package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.AuxoExecute;
import com.larryhsiao.auxo.utils.ImageToFile;
import com.larryhsiao.auxo.views.FileListCell;
import com.larryhsiao.auxo.workspace.FsFiles;
import com.larryhsiao.juno.FileByName;
import com.larryhsiao.juno.FileRenameAction;
import com.larryhsiao.juno.FilesByInput;
import com.larryhsiao.juno.QueriedAFiles;
import com.silverhetch.clotho.Source;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;

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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.TransferMode.MOVE;

/**
 * Controller of page that shows file list in Axuo.
 */
public class FileList implements Initializable {
    private final File root;
    private final Source<Connection> db;
    private final ObservableList<File> data =
        FXCollections.observableArrayList();
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
                   .addListener((observable, oldValue, newValue) -> {
                       String keyword = searchInput.textProperty().getValue();
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
                   });
        loadFiles();
        fileList.setCellFactory(param -> new FileListCell());
        fileList.setOnContextMenuRequested(event -> {
            final ContextMenu menu = new ContextMenu();
            menu.show(fileList, event.getScreenX(), event.getScreenY());
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
                event.acceptTransferModes(MOVE);
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
        fileList.setContextMenu(fileContextMenu(resources));

        Platform.runLater(() -> {
            final Window window = fileList.getScene().getWindow();
            new Thread(() -> {
                try {
                    listenForFileChange(window);
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(
                        () -> new ExceptionAlert(e, resources).fire());
                }
            }).start();
        });
    }

    private ContextMenu fileContextMenu(ResourceBundle res) {
        final ContextMenu menu = new ContextMenu();
        final MenuItem rename = new MenuItem(res.getString("rename"));
        rename.setOnAction(event -> {
            final File selected =
                fileList.getSelectionModel().getSelectedItem();
            final TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(selected.getName());
            dialog.setHeaderText(res.getString("rename"));
            dialog.setContentText(res.getString("new_name"));
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                try {
                    final File target = new File(selected.getParent(), newName);
                    Files.move(selected.toPath(), target.toPath());
                    new FileRenameAction(
                        db,
                        new FileByName(db, selected.getName()).value(),
                        newName
                    ).fire();
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
            final File selected =
                fileList.getSelectionModel().getSelectedItem();
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(selected.getName());
            alert.setContentText(MessageFormat
                .format(res.getString("delete_obj"), selected.getName()));
            alert.setHeaderText(res.getString("are_you_sure"));
            alert.setX(current.getX() + 150);
            alert.setY(current.getY() + 150);
            final Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (!selected.delete()) {
                    new ExceptionAlert(
                        new Exception("Failed to delete " + selected.getName()),
                        res).fire();
                }
            }
        });
        menu.getItems().add(delete);
        return menu;
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

    private void listenForFileChange(Window window)
        throws IOException, InterruptedException {
        try (final WatchService watchService = FileSystems.getDefault()
                                                          .newWatchService()) {
            final WatchKey watchKey = root.toPath()
                                          .register(watchService, ENTRY_CREATE,
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
                            data.add(changed.toFile());
                        } else {
                            data.remove(changed.toFile());
                        }
                    });
                }
                watchKey.reset();
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

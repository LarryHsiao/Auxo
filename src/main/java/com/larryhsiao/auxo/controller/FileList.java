package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.tagging.*;
import com.larryhsiao.auxo.utils.AuxoExecute;
import com.larryhsiao.auxo.views.FileListCell;
import com.larryhsiao.auxo.workspace.FsFiles;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.SingleConn;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
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
    private final Source<Connection> db = new SingleConn(new TagDbConn());
    private final ObservableList<File> data = FXCollections.observableArrayList();
    @FXML private TextField searchInput;
    @FXML private ListView<File> fileList;
    @FXML private AnchorPane info;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchInput.textProperty().addListener((observable, oldValue, newValue) -> {
            String keyword = searchInput.textProperty().getValue();
            Map<String, AFile> dbSearchRes = new QueriedAFiles(new FilesByKeyword(db, keyword)).value();
            data.clear();
            data.addAll(
                new FsFiles().value().entrySet().stream()
                    .filter(entry -> entry.getKey().contains(keyword) ||
                        dbSearchRes.containsKey(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).values()
            );
        });
        loadFiles();
        fileList.setCellFactory(param -> new FileListCell());
        fileList.setOnContextMenuRequested(event -> {
            final ContextMenu menu = new ContextMenu();
            menu.show(fileList, event.getScreenX(), event.getScreenY());
        });
        fileList.setItems(data);
        fileList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                if (newValue == null) {
                    return;
                }
                loadInfo(newValue, resources);
            }
        });
        fileList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                new AuxoExecute(
                    ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        fileList.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                new AuxoExecute(
                    ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        fileList.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
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
        });
        fileList.setContextMenu(fileContextMenu(resources));

        new Thread(() -> {
            try {
                listenForFileChange();
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> new ExceptionAlert(e, resources).fire());
            }
        }).start();
    }

    private ContextMenu fileContextMenu(ResourceBundle res) {
        final ContextMenu menu = new ContextMenu();
        final MenuItem delete = new MenuItem(res.getString("delete"));
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage current = ((Stage) fileList.getScene().getWindow());
                final File selected = fileList.getSelectionModel().getSelectedItem();
                final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(selected.getName());
                alert.setContentText(MessageFormat.format(res.getString("delete_obj"), selected.getName()));
                alert.setHeaderText(res.getString("are_you_sure"));
                alert.setX(current.getX() + 150);
                alert.setY(current.getY() + 150);
                final Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    if (!selected.delete()) {
                        new ExceptionAlert(new Exception("Failed to delete " + selected.getName()), res).fire();
                    }
                }
            }
        });
        menu.getItems().add(delete);
        return menu;
    }

    private void moveFileIntoWorkspace(File file) throws IOException {
        Files.move(file.toPath(), new File(
            FileSystems.getDefault().getPath(".").toFile(),
            file.getName()
        ).toPath());
    }

    private void loadFiles() {
        data.clear();
        data.addAll(new FsFiles().value().values());
    }

    private void listenForFileChange() throws IOException, InterruptedException {
        final java.nio.file.Path path = FileSystems.getDefault().getPath(".");
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            final WatchKey watchKey = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
            while (true) {
                final WatchKey wk = watchService.take();
                for (WatchEvent<?> event : wk.pollEvents()) {
                    final java.nio.file.Path changed = (java.nio.file.Path) event.context();
                    // TODO: insert/update by changed
                    Platform.runLater(this::loadFiles);
                }
                boolean valid = wk.reset();
                if (!valid) {
                    System.out.println("Key has been unregisterede");
                }
            }
        }
    }

    private void loadInfo(File selected, ResourceBundle res) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/larryhsiao/auxo/file.fxml"), res);
            loader.setController(new FileInfo(
                    new FileByName(
                        db,
                        selected.getName()
                    ).value().id()
                )
            );
            info.getChildren().clear();
            info.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

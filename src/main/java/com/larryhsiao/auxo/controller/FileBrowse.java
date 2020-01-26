package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.*;
import com.larryhsiao.auxo.views.FileListCell;
import com.silverhetch.clotho.file.FileDelete;
import com.silverhetch.clotho.file.FileText;
import com.silverhetch.clotho.log.Log;
import com.silverhetch.clotho.log.PhantomLog;
import com.silverhetch.clotho.regex.IsUrl;
import com.silverhetch.clotho.utility.comparator.StringComparator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import okhttp3.OkHttpClient;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.SimpleEditableStyledDocument;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.scene.control.ButtonType.OK;
import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.TransferMode.COPY;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Controller of file browsing.
 */
public class FileBrowse implements Initializable {
    private final OkHttpClient client;
    private final Log log;
    private final File root;
    private final File target;
    private File dirFile;
    @FXML
    private ListView<File> listView;
    @FXML
    private AnchorPane contents;

    public FileBrowse(OkHttpClient client, Log log, File root, File target) {
        this.client = client;
        this.log = log;
        this.root = root;
        this.target = target;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDirectory(target);
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                openSelectedFile(resources);
            }
        });
        listView.setCellFactory(param -> new FileListCell(log));
        listView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                try {
                    final String mimeType = new FileMimeType(newValue).value();
                    SingleMediaPlayer.release();
                    if (mimeType.startsWith("image")) {
                        loadImage(newValue, resources);
                    } else if (mimeType.startsWith("video")) {
                        loadMedia(newValue, resources);
                    } else if (mimeType.startsWith("text")) {
                        loadText(newValue, resources);
                    }
                } catch (IOException e) {
                    contents.getChildren().clear();
                }
            });

        listView.setContextMenu(contextMenu(resources));
        listView.setOnContextMenuRequested(event -> {
            listView.getContextMenu().getItems().clear();
            listView.getContextMenu().getItems().addAll(
                contextMenu(resources).getItems()
            );
        });
        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                openSelectedFile(resources);
            }
        });
        listView.setOnDragOver(event -> {
            event.acceptTransferModes(COPY);
            event.consume();
        });
        listView.setOnDragDropped(event -> {
            var board = event.getDragboard();
            if (board.hasFiles()) {
                for (var file : board.getFiles()) {
                    new Thread(() -> {
                        try {
                            Files.move(file.toPath(), new File(
                                dirFile,
                                file.getName()
                            ).toPath());
                        } catch (IOException e) {
                            new ExceptionAlert(e, resources).fire();
                        }
                    }).start();
                }
            }
            var textType = DataFormat.lookupMimeType("text/plain");
            if (board.hasContent(textType) &&
                new IsUrl(board.getContent(textType).toString(), log).value()) {
                var url = board.getContent(textType).toString();
                listView.getItems()
                    .add(new UrlFile(target, client, url).value());
            }
        });
    }

    private ContextMenu contextMenu(ResourceBundle res) {
        final ContextMenu menu = new ContextMenu();
        final MenuItem createFile = new MenuItem();
        createFile.setText(res.getString("create_file"));
        createFile.setGraphic(new MenuIcon("/images/file.png").value());
        createFile.setOnAction(event -> {
            try {
                var result = new TextInputDialog("").showAndWait();
                if (result.isPresent()) {
                    var newFile = new File(target, result.get());
                    newFile.createNewFile();
                    listView.getItems().add(newFile);
                }
            } catch (IOException e) {
                new ExceptionAlert(e, res).fire();
            }
        });
        menu.getItems().add(createFile);
        var selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            final MenuItem delete = new MenuItem();
            delete.setText(res.getString("delete"));
            delete.setGraphic(new MenuIcon("/images/trash.png").value());
            delete.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText(selected.getName());
                alert.setContentText(
                    MessageFormat.format(
                        res.getString("delete_obj"),
                        selected.getName()
                    )
                );
                var result = alert.showAndWait();
                if (result.isPresent() && result.get() == OK) {
                    new FileDelete(selected).fire();
                    listView.getItems().remove(selected);
                }
            });
            menu.getItems().add(delete);
        }
        return menu;
    }

    private void loadText(File fsFile, ResourceBundle resources) {
        StyledTextArea<String, Boolean> area = new StyledTextArea<>(
            "", (t, s) -> {
        }, false, (t, s) -> {
        }, new SimpleEditableStyledDocument<>("", false), true
        );
        area.setWrapText(true);
        area.setPrefSize(500.0, 300.0);
        area.insertText(0, new FileText(fsFile).value());
        contents.getChildren().clear();
        contents.getChildren().add(area);
        VBox.setVgrow(contents, ALWAYS);
    }

    private void openSelectedFile(ResourceBundle res) {
        final File selected = listView.getSelectionModel().getSelectedItem();
        if (selected.isDirectory()) {
            loadDirectory(selected);
        } else {
            new Thread(() -> {
                try {
                    new PlatformExecute(selected).fire();
                } catch (Exception e) {
                    Platform.runLater(() -> new ExceptionAlert(e, res).fire());
                }
            }).start();
        }
    }

    private void loadMedia(File fsFile, ResourceBundle res) throws IOException {
        final FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/larryhsiao/auxo/video_player.fxml"),
            res
        );
        loader.setController(new Player(fsFile.toURI().toASCIIString()));
        final Parent rootView = loader.load();
        contents.getChildren().clear();
        contents.getChildren().add(rootView);
        VBox.setVgrow(contents, ALWAYS);
    }

    private void loadImage(File fsFile, ResourceBundle res) {
        final ImageView imageView = new ImageView(
            new Image(fsFile.toURI().toASCIIString(), true)
        );
        imageView.setPreserveRatio(true);
        contents.getChildren().clear();
        contents.getChildren().add(imageView);
        imageView.fitHeightProperty().bind(contents.heightProperty());
        imageView.fitWidthProperty().bind(contents.widthProperty());
        VBox.setVgrow(listView, ALWAYS);
    }

    private void loadDirectory(File dir) {
        try {
            dirFile = dir;
            listView.getItems().clear();
            if (!dir.getCanonicalPath().equals(target.getCanonicalPath())) {
                listView.getItems().add(new File(dir, ".."));
            }
            final File[] children = dir.listFiles();
            if (children == null) {
                return;
            }
            List<File> files = Arrays.asList(children);
            files.sort(new FileComparator((o1, o2) -> new StringComparator()
                .compare(o2.getName(), o1.getName())));
            listView.getItems().addAll(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

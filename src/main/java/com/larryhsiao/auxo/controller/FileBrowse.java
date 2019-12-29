package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.FileComparator;
import com.larryhsiao.auxo.utils.FileMimeType;
import com.larryhsiao.auxo.utils.PlatformExecute;
import com.larryhsiao.auxo.utils.SingleMediaPlayer;
import com.larryhsiao.auxo.views.FileListCell;
import com.silverhetch.clotho.utility.comparator.StringComparator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Controller of file browsing.
 */
public class FileBrowse implements Initializable {
    private final File root;
    private final File target;
    @FXML private ListView<File> listView;
    @FXML private AnchorPane contents;

    public FileBrowse(File root, File target) {
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
        listView.setCellFactory(param -> new FileListCell());
        listView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                try {
                    final String mimeType = new FileMimeType(newValue).value();
                    SingleMediaPlayer.release();
                    if (mimeType.startsWith("image")) {
                        loadImage(newValue, resources);
                    } else if (mimeType.startsWith("video")) {
                        loadMedia(newValue, resources);
                    }
                }catch (IOException e){
                    contents.getChildren().clear();
                }
            });

        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                openSelectedFile(resources);
            }
        });
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

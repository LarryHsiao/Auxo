package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.FileMimeType;
import com.larryhsiao.auxo.views.TagListCell;
import com.larryhsiao.auxo.views.TagStringConverter;
import com.larryhsiao.juno.*;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.file.IsImage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Controller to show File details.
 */
public class FileInfo implements Initializable {
    private final File root;
    private final long fileId;
    private final ObservableList<Tag> tags =
        FXCollections.observableArrayList();
    private final Map<String, Tag> tagMap = new HashMap<>();
    private final Source<Connection> db;
    private MediaPlayer player = null;
    @FXML private TextField fileName;
    @FXML private ListView<Tag> tagList;
    @FXML private TextField newTagInput;
    @FXML private AnchorPane contents;

    public FileInfo(File root, Source<Connection> db, long fileId) {
        this.root = root;
        this.fileId = fileId;
        this.db = db;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        newTagInput.setOnAction(event -> {
            final Tag tag = new TagByName(
                db,
                newTagInput.textProperty().getValue()
            ).value();
            new AttachAction(
                db,
                fileId,
                tag.id()
            ).fire();
            tags.add(tag);
            tagMap.put(tag.name(), tag);
            newTagInput.setText("");
        });
        tagMap.putAll(new QueriedTags(new TagsByFileId(db, fileId)).value());
        tags.addAll(tagMap.values());
        tagList.setItems(tags);
        tagList.setCellFactory(param -> new TagListCell());
        tagList.setContextMenu(tagContextMenu(resources));
        fileName.setText(
            new QueriedAFile(
                new FileById(db, fileId)
            ).value().name()
        );
        tagList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                final Tag selected =
                    tagList.getSelectionModel().getSelectedItem();
                try {
                    openTagFileDialog(selected, resources);
                } catch (IOException e) {
                    new ExceptionAlert(e, resources).fire();
                }
            }
        });
        TextFields.bindAutoCompletion(newTagInput, param -> new QueriedTags(
            new TagsByKeyword(
                db, param.getUserText()
            )
        ).value().values().stream()
         .filter(tag -> !tagMap.containsKey(tag.name()))
         .collect(Collectors.toList()), new TagStringConverter(db));

        final File fsFile = new File(
            root,
            new QueriedAFile(new FileById(db, fileId)).value().name()
        );
        loadContent(fsFile, resources);
    }

    private void loadContent(File fsFile, ResourceBundle resources) {
        try {
            final String mimeType = new FileMimeType(fsFile).value();
            if (fsFile.isDirectory()) {
                loadDirectory(fsFile, resources);
            } else if (new IsImage(fsFile).value()) {
                loadImage(fsFile, resources);
            } else if (mimeType.startsWith("video") ||
                mimeType.startsWith("audio")) {
                loadMedia(fsFile, resources);
            } else {
                VBox.setVgrow(tagList, ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            VBox.setVgrow(tagList, ALWAYS);
        }
    }

    private void loadMedia(File fsFile, ResourceBundle res) throws IOException {
        if (player != null) {
            player.stop();
        }
        final FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/larryhsiao/auxo/video_player.fxml"),
            res
        );
        player = new MediaPlayer(new Media(fsFile.toURI().toASCIIString()));
        loader.setController(new Player(player));
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
        VBox.setVgrow(contents, ALWAYS);
    }

    private void loadDirectory(File fsFile, ResourceBundle resources) {
        try {
            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/larryhsiao/auxo/file_browse.fxml"),
                resources
            );
            loader.setController(new FileBrowse(root, fsFile));
            contents.getChildren().clear();
            contents.getChildren().add(loader.load());
            VBox.setVgrow(contents, ALWAYS);
        } catch (IOException e) {
            new ExceptionAlert(e, resources).fire();
        }
    }

    private void openTagFileDialog(Tag selected, ResourceBundle res)
        throws IOException {
        final Stage currentStage = ((Stage) tagList.getScene().getWindow());
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/larryhsiao/auxo/tag_files.fxml"), res);
        loader.setController(new TagFiles(
            root, db, selected.id())
        );
        final Stage newStage = new Stage();
        newStage.setMinHeight(720);
        newStage.setTitle(selected.name());
        newStage.setScene(new Scene(loader.load()));
        newStage.setX(currentStage.getX() + 100);
        newStage.setY(currentStage.getY() + 100);
        newStage.addEventHandler(KeyEvent.KEY_RELEASED,
            new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        newStage.close();
                    }
                }
            });
        newStage.show();
    }

    private ContextMenu tagContextMenu(ResourceBundle resource) {
        ContextMenu menu = new ContextMenu();
        MenuItem delete = new MenuItem();
        delete.setText(resource.getString("delete"));
        delete.setOnAction(event -> {
            final Tag selectedTag =
                tagList.getSelectionModel().getSelectedItem();
            new DetachAction(
                db, fileId, selectedTag.id()
            ).fire();
            tagList.getItems().remove(selectedTag);
        });
        menu.getItems().add(delete);
        return menu;
    }
}

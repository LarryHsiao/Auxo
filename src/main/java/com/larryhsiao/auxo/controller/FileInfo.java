package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.tagging.*;
import com.larryhsiao.auxo.views.TagListCell;
import com.larryhsiao.auxo.views.TagStringConverter;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.SingleConn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static javafx.scene.input.MouseButton.PRIMARY;

/**
 * Controller to show File details.
 */
public class FileInfo implements Initializable {
    private final long fileId;
    private final ObservableList<Tag> tags = FXCollections.observableArrayList();
    private final Map<String, Tag> tagMap = new HashMap<>();
    private final Source<Connection> db = new SingleConn(new TagDbConn());
    @FXML private TextField fileName;
    @FXML private ListView<Tag> tagList;
    @FXML private TextField newTagInput;
    @FXML private AnchorPane contents;

    public FileInfo(long fileId) {
        this.fileId = fileId;
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
                final Tag selected = tagList.getSelectionModel().getSelectedItem();
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
        ).value().values().stream().filter(tag -> !tagMap.containsKey(tag.name())).collect(Collectors.toList()), new TagStringConverter(db));

        final File fsFile = new File(
            FileSystems.getDefault().getPath(".").toFile(),
            new QueriedAFile(new FileById(db, fileId)).value().name()
        );
        if (fsFile.isDirectory()) {
            loadContent(fsFile, resources);
            VBox.setVgrow(contents, Priority.ALWAYS);
        } else {
            VBox.setVgrow(tagList, Priority.ALWAYS);
        }
    }

    private void loadContent(File fsFile, ResourceBundle resources) {
        try {
            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/larryhsiao/auxo/file_browse.fxml")
            );
            loader.setController(new FileBrowse(fsFile));
            contents.getChildren().clear();
            contents.getChildren().add(loader.load());
        } catch (IOException e) {
            new ExceptionAlert(e, resources).fire();
        }
    }

    private void openTagFileDialog(Tag selected, ResourceBundle res) throws IOException {
        final Stage currentStage = ((Stage) tagList.getScene().getWindow());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/larryhsiao/auxo/tag_files.fxml"), res);
        loader.setController(new TagFiles(
            selected.id())
        );
        final Stage newStage = new Stage();
        newStage.setMinHeight(720);
        newStage.setTitle(selected.name());
        newStage.setScene(new Scene(loader.load()));
        newStage.setX(currentStage.getX() + 100);
        newStage.setY(currentStage.getY() + 100);
        newStage.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
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
            final Tag selectedTag = tagList.getSelectionModel().getSelectedItem();
            new DetachAction(
                db, fileId, selectedTag.id()
            ).fire();
            tagList.getItems().remove(selectedTag);
        });
        menu.getItems().add(delete);
        return menu;
    }
}

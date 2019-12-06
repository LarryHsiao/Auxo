package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.tagging.*;
import com.larryhsiao.auxo.views.TagListCell;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.SingleConn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

/**
 * Controller to show File details.
 */
public class FileInfo implements Initializable {
    private final long fileId;
    private final ObservableList<Tag> tags = FXCollections.observableArrayList();
    private final Source<Connection> db = new SingleConn(new TagDbConn());
    @FXML private Label fileName;
    @FXML private ListView<Tag> tagList;
    @FXML private TextField newTagInput;

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
            newTagInput.setText("");
        });
        tags.addAll(
            new QueriedTags(
                new TagsByFileId(db, fileId)
            ).value().values()
        );
        tagList.setItems(tags);
        tagList.setCellFactory(param -> new TagListCell());
        tagList.setContextMenu(tagContextMenu(resources));
        fileName.setText(
            new QueriedAFile(
                new FileById(db, fileId)
            ).value().name()
        );
        TextFields.bindAutoCompletion(newTagInput, param -> new QueriedTags(
            new TagsByKeyword(
                db, param.getUserText()
            )
        ).value().values(), new StringConverter<Tag>() {
            @Override
            public String toString(Tag object) {
                return object.name();
            }

            @Override
            public Tag fromString(String string) {
                return new TagByName(db, string).value();
            }
        });
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

package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.tagging.*;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.SingleConn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

/**
 * Controller to show File details.
 */
public class FileInfo implements Initializable {
    private final long fileId;
    private final ObservableList<String> tags = FXCollections.observableArrayList();
    private final Source<Connection> db = new SingleConn(new TagDbConn());
    @FXML private Label fileName;
    @FXML private ListView<String> tagList;
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
            tags.add(tag.name());
        });
        tags.addAll(
            new QueriedTags(
                new TagsByFileId(db, fileId)
            ).value().keySet()
        );
        tagList.setItems(tags);
        fileName.setText(
            new QueriedAFile(
                new FileById(db, fileId)
            ).value().name()
        );
    }
}

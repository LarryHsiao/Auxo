package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.tagging.*;
import com.silverhetch.clotho.database.SingleConn;
import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller to show File details.
 */
public class FileInfo implements Initializable {
    private final long fileId;
    private final ObservableList<String> tags = FXCollections.observableArrayList();
    @FXML private Label fileName;
    @FXML private ListView<String> tagList;

    public FileInfo(long fileId) {
        this.fileId = fileId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tags.addAll(
            new QueriedTags(
                new TagsByFileId(
                    new SingleConn(
                        new TagDbConn()
                    ),
                    1
                )
            ).value().keySet()
        );
        tagList.setItems(tags);
        fileName.setText(
            new QueriedAFile(
                new FileById(
                    new SingleConn(
                        new TagDbConn()
                    ),
                    fileId
                )
            ).value().name()
        );
    }
}

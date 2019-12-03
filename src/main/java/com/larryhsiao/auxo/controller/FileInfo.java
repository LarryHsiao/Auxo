package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.tagging.*;
import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller to show File details.
 */
public class FileInfo implements Initializable {
    private long fileId;
    @FXML
    private Label fileName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileName.setText(
            new QueriedAFile(
                new FileById(
                    new FakeDataConn(
                        new TagDbConn(new InMemoryConn())
                    ),
                    2
                )
            ).value().name()
        );
    }

    public void init(long fileId) {
        this.fileId = fileId;
    }
}

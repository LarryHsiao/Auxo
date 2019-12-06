package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.tagging.*;
import com.larryhsiao.auxo.utils.FileOpening;
import com.larryhsiao.auxo.views.FileListCell;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.SingleConn;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.MouseButton.PRIMARY;

/**
 * Controller for file list that have given tag attached.
 */
public class TagFiles implements Initializable {
    private final Source<Connection> db = new SingleConn(new TagDbConn());
    private final long tagId;
    @FXML private ListView<File> fileList;

    public TagFiles(long tagId) {
        this.tagId = tagId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileList.setCellFactory(param -> new FileListCell());
        fileList.getItems().addAll(
            new QueriedAFiles(
                new FilesByTagId(db, tagId)
            ).value().values().stream()
                .map(aFile -> new File(
                    FileSystems.getDefault().getPath(".").toFile(),
                    aFile.name()
                )).collect(Collectors.toList())
        );

        fileList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                new FileOpening(
                    ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        fileList.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                new FileOpening(
                    ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
    }
}

package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.workspace.FsFiles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.input.MouseButton.PRIMARY;

/**
 * Controller of page that shows file list in Axuo.
 */
public class FileList implements Initializable {
    private final ObservableList<File> data = FXCollections.observableArrayList();
    @FXML
    private ListView<File> fileList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        data.addAll(new FsFiles().value().values());
        fileList.setItems(data);
        fileList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
//                    fileList.getSelectionModel().getSelectedItems()
            }
        });
    }
}

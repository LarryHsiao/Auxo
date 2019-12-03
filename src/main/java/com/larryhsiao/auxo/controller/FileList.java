package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.workspace.FsFiles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller of page that shows file list in Axuo.
 */
public class FileList implements Initializable {
    private final ObservableList<String> data = FXCollections.observableArrayList();
    @FXML
    private ListView<String> fileList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        data.addAll(new FsFiles().value().keySet());
        fileList.setItems(data);
    }
}

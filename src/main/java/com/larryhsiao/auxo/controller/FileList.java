package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.tagging.FileByName;
import com.larryhsiao.auxo.tagging.TagDbConn;
import com.larryhsiao.auxo.workspace.FsFiles;
import com.silverhetch.clotho.database.SingleConn;
import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.input.MouseButton.PRIMARY;

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
        fileList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                try {
                    final FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/larryhsiao/auxo/file.fxml"));
                    loader.setController(new FileInfo(
                            new FileByName(
                                new SingleConn(new TagDbConn()),
                                fileList.getSelectionModel().getSelectedItem()
                            ).value().id()
                        )
                    );
                    Stage stage = new Stage();
                    stage.setScene(new Scene(loader.load()));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

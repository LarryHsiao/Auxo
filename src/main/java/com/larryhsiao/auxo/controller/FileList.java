package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.tagging.FileByName;
import com.larryhsiao.auxo.tagging.TagDbConn;
import com.larryhsiao.auxo.workspace.FsFiles;
import com.silverhetch.clotho.database.SingleConn;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static javafx.scene.input.MouseButton.PRIMARY;

/**
 * Controller of page that shows file list in Axuo.
 */
public class FileList implements Initializable {
    private final ObservableList<String> data = FXCollections.observableArrayList();
    @FXML private TextField searchInput;
    @FXML private ListView<String> fileList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchInput.textProperty().addListener((observable, oldValue, newValue) -> {
            data.clear();
            data.addAll(
                new FsFiles().value().entrySet().stream()
                    .filter(entry -> entry.getKey().contains(searchInput.textProperty().getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet()
            );
        });
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
                    Stage current = ((Stage) fileList.getScene().getWindow());
                    Stage newStage = new Stage();
                    newStage.setScene(new Scene(loader.load()));
                    newStage.setX(current.getX() + 50);
                    newStage.setY(current.getY() + 50);
                    newStage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

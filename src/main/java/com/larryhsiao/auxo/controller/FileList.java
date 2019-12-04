package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.tagging.*;
import com.larryhsiao.auxo.utils.Execute;
import com.larryhsiao.auxo.workspace.FsFiles;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.SingleConn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static javafx.scene.input.MouseButton.PRIMARY;

/**
 * Controller of page that shows file list in Axuo.
 */
public class FileList implements Initializable {
    private final Source<Connection> db = new SingleConn(new TagDbConn());
    private final ObservableList<File> data = FXCollections.observableArrayList();
    @FXML private TextField searchInput;
    @FXML private ListView<File> fileList;
    @FXML private AnchorPane info;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchInput.textProperty().addListener((observable, oldValue, newValue) -> {
            String keyword = searchInput.textProperty().getValue();
            Map<String, AFile> dbSearchRes = new QueriedAFiles(new FilesByKeyword(db, keyword)).value();
            data.clear();
            data.addAll(
                new FsFiles().value().entrySet().stream()
                    .filter(entry -> entry.getKey().contains(keyword) ||
                        dbSearchRes.containsKey(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).values()
            );
        });
        data.addAll(new FsFiles().value().values());
        fileList.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            @Override
            public ListCell<File> call(ListView<File> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty ) {
                            setText(item.getName());
                            loadImage(item);
                        }
                    }

                    private void loadImage(File item) {
                        try {
                            if ("image/png".equals(Files.probeContentType(item.toPath()))) {
                                final ImageView imageView = new ImageView(item.toURI().toASCIIString());
                                imageView.setPreserveRatio(true);
                                imageView.setFitHeight(75);
                                setGraphic(imageView);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
            }
        });
        fileList.setItems(data);
        fileList.setOnMouseClicked(event -> {
            final File selectedFile = fileList.getSelectionModel().getSelectedItem();
            loadInfo(selectedFile);
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                new Thread(() -> new Execute(selectedFile).fire()).start();
            }
        });
    }

    private void loadInfo(File selected) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/larryhsiao/auxo/file.fxml"));
            loader.setController(new FileInfo(
                    new FileByName(
                        new SingleConn(new TagDbConn()),
                        selected.getName()
                    ).value().id()
                )
            );
            info.getChildren().clear();
            info.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

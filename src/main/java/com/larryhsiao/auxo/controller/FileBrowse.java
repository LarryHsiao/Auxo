package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.Execute;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.scene.input.MouseButton.PRIMARY;

/**
 * Controller of file browsing.
 */
public class FileBrowse implements Initializable {
    private final File target;
    @FXML private ListView<File> listView;

    public FileBrowse(File target) {
        this.target = target;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDirectory(target);
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                openSelectedFile(resources);
            }
        });
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<File> call(ListView<File> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });

        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                openSelectedFile(resources);
            }
        });
    }

    private void openSelectedFile(ResourceBundle res) {
        final File selected = listView.getSelectionModel().getSelectedItem();
        if (selected.isDirectory()) {
            loadDirectory(selected);
        } else {
            new Thread(() -> {
                try {
                    new Execute(selected).fire();
                } catch (Exception e) {
                    Platform.runLater(() -> new ExceptionAlert(e, res).fire());
                }
            }).start();
        }
    }

    private void loadDirectory(File dir) {
        try {
            listView.getItems().clear();
            if (!dir.getCanonicalPath().equals(target.getCanonicalPath())) {
                listView.getItems().add(new File(dir, ".."));
            }
            listView.getItems().addAll(dir.listFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

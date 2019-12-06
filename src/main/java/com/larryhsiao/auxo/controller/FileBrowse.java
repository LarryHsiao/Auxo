package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.FileComparator;
import com.larryhsiao.auxo.utils.PlatformExecute;
import com.larryhsiao.auxo.views.FileListCell;
import com.silverhetch.clotho.utility.comparator.StringComparator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
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
        listView.setCellFactory(param -> new FileListCell());

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
                    new PlatformExecute(selected).fire();
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
            final File[] children = dir.listFiles();
            if (children == null) {
                return;
            }
            List<File> files = Arrays.asList(children);
            files.sort(new FileComparator((o1, o2) -> new StringComparator().compare(o2.getName(), o1.getName())));
            listView.getItems().addAll(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

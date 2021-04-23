package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.utils.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.views.ZipEntryCell;
import com.larryhsiao.clotho.log.Log;
import com.larryhsiao.clotho.utility.comparator.StringComparator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * Controller fot zip browsing.
 */
public class ZipBrowse implements Initializable {
    private final Log log;
    private final ZipFile zipFile;
    private List<String> subDirStack;
    @FXML private ListView<String> listView;
    @FXML private AnchorPane contents;

    public ZipBrowse(Log log, ZipFile zipFile) {
        this.log = log;
        this.zipFile = zipFile;
        this.subDirStack = new ArrayList<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle res) {
        listView.setCellFactory(param -> new ZipEntryCell(log, zipFile));
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            try {
//                if (newValue == null) {
//                    contents.getChildren().clear();
//                    return;
//                }
//                var contentType = Files.probeContentType(
//                    Path.of(newValue));
//                if (contentType != null && contentType.startsWith("image")) {
//                    loadImage(zipFile.getEntry(newValue), res);
//                }
//            } catch (Exception e) {
//                new ExceptionAlert(e, res).fire();
//            }
        });
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() >= 2 && event.getButton() == PRIMARY) {
                var selected = listView.getSelectionModel().getSelectedItem();
                if ("..".equals(selected)) {
                    subDirStack.remove(subDirStack.size() - 1);
                    loadFileList(res);
                }
                if (zipFile.getEntry(selected).isDirectory()) {
                    subDirStack.add(selected);
                    loadFileList(res);
                }
            }
        });
        loadFileList(res);
    }

    private void loadFileList(ResourceBundle res) {
        listView.getItems().clear();
        if (subDirStack.size() > 0) {
            listView.getItems().add("..");
        }
        zipFile.entries().asIterator().forEachRemaining((Consumer<ZipEntry>) entry -> {
            if (subDirStack.size() == 0 && entry.getName().split("/").length <= 1) {
                listView.getItems().add(entry.getName());
            }
            if (subDirStack.size() > 0) {
                var currentDir = zipFile.getEntry(subDirStack.get(subDirStack.size() - 1));
                if (entry.getName().startsWith(currentDir.getName()) &&
                    entry.getName().replace(currentDir.getName(), "")
                        .split("/").length <= 1 &&
                    !currentDir.getName().equals(entry.getName())) {
                    listView.getItems().add(entry.getName());
                }
            }
        });
        var comparator = new StringComparator();
        listView.getItems().sort((o1, o2) -> {
            if (o1.equals("..")) {
                return -1;
            }
            var entry1 = zipFile.getEntry(o1);
            var entry2 = zipFile.getEntry(o2);
            if (entry1.isDirectory() && !entry2.isDirectory()) {
                return -1;
            } else if (!entry1.isDirectory() && entry2.isDirectory()) {
                return 1;
            } else {
                return comparator.compare(entry2.getName(), entry1.getName());
            }
        });
    }

    private void loadImage(ZipEntry selected, ResourceBundle res) throws Exception {
        final ImageView imageView = new ImageView(
            new Image(zipFile.getInputStream(selected))
        );
        imageView.setPreserveRatio(true);
        contents.getChildren().clear();
        contents.getChildren().add(imageView);
        imageView.fitHeightProperty().bind(contents.heightProperty());
        imageView.fitWidthProperty().bind(contents.widthProperty());
        VBox.setVgrow(listView, ALWAYS);
    }
}

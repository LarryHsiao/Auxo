package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.silverhetch.clotho.Source;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

/**
 * Controller for entry page of Auxo.
 */
public class Main implements Initializable {
    private static final int PAGE_TAG_MANAGEMENT = 1;
    private static final int PAGE_FILE_MANAGEMENT = 2;
    private static final int PAGE_DEVICES = 3;

    private final File root;
    private final Source<Connection> db;
    private int currentPage = -1;
    private Object currentPageController = null;
    @FXML private Button tagManagement;
    @FXML private Button fileManagement;
    @FXML private Button devices;
    @FXML private AnchorPane content;

    public Main(File root, Source<Connection> db) {
        this.root = root;
        this.db = db;
    }

    @Override
    public void initialize(URL location, ResourceBundle res) {
        tagManagement.setText(res.getString("tag_management"));
        tagManagement.setOnAction(event -> loadTagManagement(res));
        fileManagement.setText(res.getString("file_management"));
        fileManagement.setOnAction(event -> loadFileList(res));
        loadFileList(res);
        fileManagement.setOnAction(event -> loadFileList(res));
        devices.setText(res.getString("devices"));
        devices.setOnAction(event -> loadDevices(res));
        loadFileList(res);
    }

    private void loadDevices(ResourceBundle res) {
        try {
            if (currentPage == PAGE_DEVICES) {
                return;
            }
            tearDownCurrentController(res);
            currentPage = PAGE_DEVICES;
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/larryhsiao/auxo/devices.fxml"),
                res
            );
            Parent parent = loader.load();
            currentPageController = loader.getController();
            content.getChildren().clear();
            content.getChildren().add(parent);
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    private void loadFileList(ResourceBundle res) {
        try {
            if (currentPage == PAGE_FILE_MANAGEMENT) {
                return;
            }
            tearDownCurrentController(res);
            currentPage = PAGE_FILE_MANAGEMENT;
            content.getChildren().clear();
            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/larryhsiao/auxo/file_list.fxml"),
                res
            );
            loader.setController(new FileList(root, db));
            content.getChildren().add(loader.load());
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    private void loadTagManagement(ResourceBundle res) {
        try {
            if (currentPage == PAGE_TAG_MANAGEMENT) {
                return;
            }
            tearDownCurrentController(res);
            currentPage = PAGE_TAG_MANAGEMENT;
            content.getChildren().clear();

            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/larryhsiao/auxo/tags.fxml"),
                res
            );
            loader.setController(new TagList(root, db));
            content.getChildren().add(loader.load());
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    private void tearDownCurrentController(ResourceBundle res){
        try {
            if (currentPageController != null && currentPageController instanceof Closeable) {
                ((Closeable) currentPageController).close();
            }
            currentPageController = null;
        }catch (IOException e){
            new ExceptionAlert(e, res).fire();
        }
    }
}

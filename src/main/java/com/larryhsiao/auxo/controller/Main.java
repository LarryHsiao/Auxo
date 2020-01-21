package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.controller.devices.Devices;
import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.silverhetch.clotho.Source;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.stage.Stage;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.layout.BackgroundRepeat.NO_REPEAT;
import static javafx.scene.layout.BackgroundSize.DEFAULT;
import static javafx.stage.StageStyle.UNDECORATED;

/**
 * Controller for entry page of Auxo.
 */
public class Main implements Initializable, Closeable {
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
    @FXML private Button about;
    @FXML private AnchorPane content;

    public Main(File root, Source<Connection> db) {
        this.root = root;
        this.db = db;
    }

    @Override
    public void initialize(URL location, ResourceBundle res) {
        tagManagement
            .setBackground(new Background(new BackgroundImage(new Image(
                getClass().getResource("/images/tag.png").toExternalForm()),
                NO_REPEAT, NO_REPEAT,
                BackgroundPosition.DEFAULT, DEFAULT)));
        tagManagement.setPrefHeight(45);
        tagManagement.setMaxWidth(45);
        tagManagement.setOnAction(event -> loadTagManagement(res));
        fileManagement
            .setBackground(new Background(new BackgroundImage(new Image(
                getClass().getResource("/images/file.png").toExternalForm()),
                NO_REPEAT, NO_REPEAT,
                BackgroundPosition.DEFAULT, DEFAULT)));
        fileManagement.setPrefHeight(45);
        fileManagement.setMaxWidth(45);
        fileManagement.setOnAction(event -> loadFileList(res));
        loadFileList(res);
        devices.setBackground(new Background(new BackgroundImage(new Image(
                getClass().getResource("/images/devices.png").toExternalForm()),
                NO_REPEAT, NO_REPEAT,
                BackgroundPosition.DEFAULT, DEFAULT)));
        devices.setPrefHeight(45);
        devices.setMaxWidth(45);
        devices.setOnAction(event -> loadDevices(res));
        about.setBackground(new Background(new BackgroundImage(new Image(
            getClass().getResource("/images/about.png").toExternalForm()),
            NO_REPEAT, NO_REPEAT,
            BackgroundPosition.DEFAULT, DEFAULT)));
        about.setPrefHeight(45);
        about.setMaxWidth(45);
        about.setOnAction(event -> loadAbout(res));
        about.setText(res.getString("about"));
        loadFileList(res);
    }

    private void loadAbout(ResourceBundle res) {
        try {
            final Scene scene = new Scene(FXMLLoader.load(
                getClass().getResource("/com/larryhsiao/auxo/about.fxml"),
                res
            ));
            scene.getStylesheets().addAll(content.getScene().getStylesheets());
            final Stage stage = new Stage();
            stage.initStyle(UNDECORATED);
            stage.setScene(scene);
            stage.addEventHandler(MOUSE_PRESSED, event -> stage.close());
            stage.focusedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue) {
                        stage.close();
                    }
                });
            stage.showAndWait();
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    private void loadDevices(ResourceBundle res) {
        try {
            if (currentPage == PAGE_DEVICES) {
                return;
            }
            tearDownCurrentController(res);
            currentPage = PAGE_DEVICES;
            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/larryhsiao/auxo/devices/devices.fxml"), res);
            currentPageController = new Devices(db, root);
            loader.setController(currentPageController);
            Parent parent = loader.load();
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

    @Override
    public void close() throws IOException {
        tearDownCurrentController(null);
    }

    private void tearDownCurrentController(ResourceBundle res) {
        try {
            if (currentPageController != null &&
                currentPageController instanceof Closeable) {
                ((Closeable) currentPageController).close();
            }
            currentPageController = null;
        } catch (IOException e) {
            if (res != null) {
                new ExceptionAlert(e, res).fire();
            } else {
                e.printStackTrace();
            }
        }
    }
}

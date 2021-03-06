package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.utils.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.workspace.FsFiles;
import com.larryhsiao.juno.AllFiles;
import com.larryhsiao.juno.QueriedAFiles;
import com.larryhsiao.clotho.Source;
import com.larryhsiao.clotho.file.FileSize;
import com.larryhsiao.clotho.file.SizeText;
import com.larryhsiao.clotho.log.Log;
import com.larryhsiao.clotho.source.ConstSource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import org.controlsfx.control.StatusBar;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
    private static final int PAGE_CONFIG = 4;

    private final Log log;
    private final OkHttpClient client;
    private final Source<Connection> db;
    private int currentPage = -1;
    private Object currentPageController = null;
    private File root;

    @FXML private Button tagManagement;
    @FXML private Button fileManagement;
    @FXML private Button config;
    @FXML private Button about;
    @FXML private AnchorPane content;
    @FXML private StatusBar statusBar;
    @FXML private Button exit;

    public Main(
        Log log, OkHttpClient client, File root, Source<Connection> db) {
        this.log = log;
        this.client = client;
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
        about.setBackground(new Background(new BackgroundImage(new Image(
            getClass().getResource("/images/about.png").toExternalForm()),
            NO_REPEAT, NO_REPEAT,
            BackgroundPosition.DEFAULT, DEFAULT)));
        about.setPrefHeight(45);
        about.setMaxWidth(45);
        about.setOnAction(event -> loadAbout(res));
        about.setText(res.getString("about"));
        config.setBackground(new Background(new BackgroundImage(new Image(
            getClass().getResource("/images/config.png").toExternalForm(),
            45.0, 45.0, true, true),
            NO_REPEAT, NO_REPEAT,
            BackgroundPosition.DEFAULT, DEFAULT)));
        config.setPrefHeight(45);
        config.setMaxWidth(45);
        config.setOnAction(event -> loadConfig(res));
        loadFileList(res);

        exit.setPrefHeight(45);
        exit.setMaxWidth(45);
        exit.setBackground(new Background(new BackgroundImage(new Image(
            getClass().getResource("/images/power.png").toExternalForm(),
            45.0, 45.0, true, true),
            NO_REPEAT, NO_REPEAT,
            BackgroundPosition.DEFAULT, DEFAULT)));
        exit.setOnAction(
            actionEvent -> ((Stage) exit.getScene().getWindow()).close());

        loadStatusBar(res);
    }

    private void loadStatusBar(ResourceBundle res) {
        statusBar.setProgress(
            1 - ((double) root.getUsableSpace() / (double) root.getTotalSpace())
        );
        statusBar.setText("");
        statusBar.getLeftItems().add(new Label(
            MessageFormat.format(
                res.getString("workspace_"),
                new SizeText(new ConstSource<>(workspaceUsedSize())).value()
            )
        ));
        statusBar.getRightItems().add(new Label(
            MessageFormat.format(
                res.getString("free_"),
                new SizeText(new ConstSource<>(root.getFreeSpace())).value()
            ) + " " + MessageFormat.format(
                res.getString("total_"),
                new SizeText(new ConstSource<>(root.getTotalSpace())).value()
            )
        ));
    }

    private long workspaceUsedSize() {
        return new FsFiles(root)
            .value().entrySet().stream()
            .filter(entry ->
                new QueriedAFiles(new AllFiles(db)).value()
                    .containsKey(entry.getKey())
            ).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (u, v) -> {
                    throw new IllegalStateException(
                        String.format("Duplicate key %s", u)
                    );
                }, LinkedHashMap::new)
            ).values()
            .stream()
            .mapToLong(file -> new FileSize(file.toPath()).value()).sum();
    }

    private void loadConfig(ResourceBundle res) {
        try {
            if (currentPage == PAGE_CONFIG) {
                return;
            }
            tearDownCurrentController(res);
            currentPage = PAGE_CONFIG;
            final FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/larryhsiao/auxo/config.fxml"
            ), res);
            loader.setController(new Config(log, root));
            Parent parent = loader.load();
            content.getChildren().clear();
            content.getChildren().add(parent);
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
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
            loader.setController(new FileList(log, client, root, db));
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
            loader.setController(new TagList(log, client, root, db));
            content.getChildren().add(loader.load());
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    @Override
    public void close() {
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

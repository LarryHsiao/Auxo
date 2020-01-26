package com.larryhsiao.auxo;

import com.jfoenix.controls.JFXDecorator;
import com.larryhsiao.auxo.workspace.FsFiles;
import com.larryhsiao.juno.*;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.SingleConn;
import com.silverhetch.clotho.database.sqlite.SQLiteConn;
import com.silverhetch.clotho.log.BeautyLog;
import com.silverhetch.clotho.log.Log;
import com.silverhetch.clotho.source.ConstSource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

/**
 * Entry point of Auxo.
 */
public class Main extends Application {
    private Source<Connection> db;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final Log log = new BeautyLog().value();
        final OkHttpClient client = new OkHttpClient();
        final File root = FileSystems.getDefault().getPath(".").toFile();
//        final File root =
//            new File("/home/larryhsiao/Dropbox/Elizabeth/MediaSamples/");
//        final File root =
//            new File("/home/larryhsiao/Dropbox/carpo_nyx/");
        db = new SingleConn(new TagDbConn(root));
        moveToH2();
        new CleanUpFiles(
            db,
            new ConstSource<>(
                new ArrayList(new FsFiles(root).value().keySet())
            )
        ).fire();
        final FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/larryhsiao/auxo/main.fxml"),
//            getClass().getResource("/com/larryhsiao/auxo/tags.fxml"),
            ResourceBundle.getBundle("i18n/default")
        );
        loader.setController(new com.larryhsiao.auxo.controller.Main(log, client, root, db));
        var decorator = new JFXDecorator(stage, loader.load());
        decorator.setPrefWidth(1280);
        decorator.setPrefHeight(720);
        var scene = new Scene(decorator);
        scene.getStylesheets().addAll(
            getClass().getResource("/stylesheet/General.css").toExternalForm(),
            requireNonNull(getClass().getClassLoader()
                .getResource("com/jfoenix/assets/css/jfoenix-design.css"))
                .toExternalForm(),
            requireNonNull(getClass().getClassLoader()
                .getResource("com/jfoenix/assets/css/jfoenix-fonts.css"))
                .toExternalForm(),
            getClass().getResource("/stylesheet/default.css").toExternalForm(),
            getClass().getResource("/stylesheet/Button.css").toExternalForm(),
            getClass().getResource("/stylesheet/Decorator.css")
                .toExternalForm(),
            getClass().getResource("/stylesheet/ListView.css").toExternalForm(),
            getClass().getResource("/stylesheet/SplitPane.css")
                .toExternalForm(),
            getClass().getResource("/stylesheet/TextField.css")
                .toExternalForm(),
            getClass().getResource("/stylesheet/ToggleButton.css")
                .toExternalForm(),
            getClass().getResource("/stylesheet/ToolBar.css").toExternalForm()
        );
        stage.setTitle(root.getAbsolutePath());
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            try {
                if (loader.getController() instanceof Closeable) {
                    ((Closeable) loader.getController()).close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        var screens = Screen.getScreens();
        if (screens.size() >= 2) {
            final Rectangle2D bounds = screens.get(0).getVisualBounds();
            stage.setX(bounds.getMinX() + 100);
            stage.setY(bounds.getMinY() + 100);
        }
        stage.show();
    }
    @Override
    public void stop() throws Exception {
        super.stop();
        db.value().close();
    }

    private void moveToH2() {
        File previous = new File(".auxo.db");
        if (previous.exists()) {
            final Connection h2Conn = db.value();
            final Connection sqliteConn =
                new SingleConn(new SQLiteConn(".auxo.db")).value();
            new QueriedAFiles(new AllFiles(new ConstSource<>(sqliteConn)))
                .value().forEach((s, aFile) -> {
                AFile h2File =
                    new FileByName(new ConstSource<>(h2Conn), aFile.name())
                        .value();
                new QueriedTags(
                    new TagsByFileId(new ConstSource<>(sqliteConn), aFile.id())
                ).value().forEach((s1, tag) -> {
                    Tag h2Tag =
                        new TagByName(new ConstSource<>(h2Conn), tag.name())
                            .value();
                    new AttachAction(
                        new ConstSource<>(h2Conn),
                        h2File.id(),
                        h2Tag.id()
                    ).fire();
                });
            });
            try {
                sqliteConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                Files.move(previous.toPath(),
                    new File(".auxo.db.bak").toPath());
            } catch (Exception ignore) {
            }
        }
    }
}

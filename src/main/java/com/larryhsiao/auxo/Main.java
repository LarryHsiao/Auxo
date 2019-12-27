package com.larryhsiao.auxo;

import com.larryhsiao.auxo.workspace.FsFiles;
import com.larryhsiao.juno.*;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.SingleConn;
import com.silverhetch.clotho.database.sqlite.SQLiteConn;
import com.silverhetch.clotho.source.ConstSource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

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
//        final File root = FileSystems.getDefault().getPath(".").toFile();
        final File root = new File("/home/larryhsiao/Dropbox/Elizabeth/MediaSamples/");
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
        loader.setController(new com.larryhsiao.auxo.controller.Main(root, db));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
            getClass().getResource("/stylesheet/default.css").toExternalForm()
        );
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.setTitle(root.getAbsolutePath());
        stage.setScene(scene);
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

package com.larryhsiao.auxo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.ResourceBundle;

/**
 * Entry point of Auxo.
 */
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final File root = FileSystems.getDefault().getPath(".").toFile();
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/larryhsiao/auxo/main.fxml"),
//            getClass().getResource("/com/larryhsiao/auxo/tags.fxml"),
            ResourceBundle.getBundle("i18n/default")
        );
        loader.setController(new com.larryhsiao.auxo.controller.Main(root));
        Scene scene = new Scene(loader.load());
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.setTitle(root.getAbsolutePath());
        stage.setScene(scene);
        stage.show();
    }
}

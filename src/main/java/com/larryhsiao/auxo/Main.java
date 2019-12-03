package com.larryhsiao.auxo;

import com.larryhsiao.auxo.controller.FileInfo;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

/**
 * Entry point of Auxo.
 */
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/larryhsiao/auxo/file.fxml"));
        loader.setController(new FileInfo(1));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }
}

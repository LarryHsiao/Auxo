package com.larryhsiao.auxo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/larryhsiao/auxo/main.fxml"),
//            getClass().getResource("/com/larryhsiao/auxo/tags.fxml"),
            ResourceBundle.getBundle("i18n/default")
        );
        Scene scene = new Scene(loader.load());
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }
}

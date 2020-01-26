package com.larryhsiao.auxo.utils;

import com.larryhsiao.auxo.controller.FileBrowse;
import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.silverhetch.clotho.Action;
import com.silverhetch.clotho.log.Log;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Open given file.
 */
public class AuxoExecute implements Action {
    private final OkHttpClient client;
    private final Log log;
    private final File root;
    private final Stage currentStage;
    private final File file;
    private final ResourceBundle res;

    public AuxoExecute(
        OkHttpClient client, Log log, File root, Stage currentStage,
        File file, ResourceBundle res) {
        this.client = client;
        this.log = log;
        this.root = root;
        this.currentStage = currentStage;
        this.file = file;
        this.res = res;
    }

    @Override
    public void fire() {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            fileBrowse(file, res);
        } else {
            new Thread(() -> {
                try {
                    new PlatformExecute(file).fire();
                } catch (Exception e) {
                    Platform.runLater(() -> new ExceptionAlert(e, res).fire());
                }
            }).start();
        }
    }

    private void fileBrowse(File selected, ResourceBundle res) {
        try {
            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/larryhsiao/auxo/file_browse.fxml"),
                res);
            loader.setController(new FileBrowse(client, log, root, selected));
            final Stage newStage = new Stage();
            newStage.setTitle(selected.getName());
            final Scene scene = new Scene(loader.load());
            scene.getStylesheets()
                .addAll(currentStage.getScene().getStylesheets());
            newStage.setScene(scene);
            newStage.setX(currentStage.getX() + 100);
            newStage.setY(currentStage.getY() + 100);
            newStage.addEventHandler(KeyEvent.KEY_RELEASED,
                event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        newStage.close();
                    }
                });
            newStage.setOnHidden(event -> SingleMediaPlayer.release());
            newStage.show();
        } catch (Exception e) {
            new ExceptionAlert(e, res).fire();
        }
    }
}

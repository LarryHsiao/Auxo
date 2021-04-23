package com.larryhsiao.auxo.utils;

import com.larryhsiao.auxo.controller.FileBrowse;
import com.larryhsiao.auxo.controller.ZipBrowse;
import com.larryhsiao.auxo.utils.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.dialogs.PopupPage;
import com.larryhsiao.clotho.Action;
import com.larryhsiao.clotho.log.Log;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.ResourceBundle;
import java.util.zip.ZipFile;

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
        } else if (file.getName().endsWith(".zip")) {
            browseZip(res, file);
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

    private void browseZip(ResourceBundle res, File selected) {
        try {
            var loader = new FXMLLoader(getClass().getResource("/com/larryhsiao/auxo/file_browse.fxml"), res);
            loader.setController(new ZipBrowse(log, new ZipFile(selected)));
            new PopupPage(res, loader.load(), currentStage, selected).fire();
        } catch (Exception e) {
            new ExceptionAlert(e, res).fire();
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

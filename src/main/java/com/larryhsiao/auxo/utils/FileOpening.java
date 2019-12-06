package com.larryhsiao.auxo.utils;

import com.larryhsiao.auxo.controller.FileBrowse;
import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.silverhetch.clotho.Action;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Open given file.
 */
public class FileOpening implements Action {
    private final Stage currentStage;
    private final File file;
    private final ResourceBundle res;

    public FileOpening(Stage currentStage, File file, ResourceBundle res) {
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
                    new Execute(file).fire();
                } catch (Exception e) {
                    Platform.runLater(() -> new ExceptionAlert(e, res).fire());
                }
            }).start();
        }
    }

    private void fileBrowse(File selected, ResourceBundle res) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/larryhsiao/auxo/file_browse.fxml"), res);
            loader.setController(new FileBrowse(selected));
            final Stage newStage = new Stage();
            newStage.setTitle(selected.getName());
            newStage.setScene(new Scene(loader.load()));
            newStage.setX(currentStage.getX() + 100);
            newStage.setY(currentStage.getY() + 100);
            newStage.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        newStage.close();
                    }
                }
            });
            newStage.show();
        } catch (Exception e) {
            new ExceptionAlert(e, res).fire();
        }
    }
}
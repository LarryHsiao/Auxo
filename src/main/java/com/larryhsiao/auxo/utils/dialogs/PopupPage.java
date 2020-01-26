package com.larryhsiao.auxo.utils.dialogs;

import com.silverhetch.clotho.Action;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Action for showing popup dialog.
 */
public class PopupPage implements Action {
    private final ResourceBundle res;
    private final Parent newNode;
    private final Stage current;
    private final File file;

    public PopupPage(ResourceBundle res, Parent newNode, Stage current, File file) {
        this.res = res;
        this.newNode = newNode;
        this.current = current;
        this.file = file;
    }

    @Override
    public void fire() {
        try {
            final Stage newStage = new Stage();
            newStage.setTitle(file.getName());
            final Scene scene = new Scene(newNode);
            scene.getStylesheets().addAll(current.getScene().getStylesheets());
            newStage.setScene(scene);
            newStage.setX(current.getX() + 100);
            newStage.setY(current.getY() + 100);
            newStage.addEventHandler(KeyEvent.KEY_RELEASED,
                event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        newStage.close();
                    }
                });
            newStage.show();
        } catch (Exception e) {
            new ExceptionAlert(e, res).fire();
        }
    }
}

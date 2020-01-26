package com.larryhsiao.auxo.utils.dialogs;

import com.silverhetch.clotho.Action;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

/**
 * Alert Dialog to show given exception.
 */
public class ExceptionAlert implements Action {
    private final Exception exception;
    private final ResourceBundle res;

    public ExceptionAlert(Exception exception, ResourceBundle res) {
        this.exception = exception;
        this.res = res;
    }

    @Override
    public void fire() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(res.getString("error"));
        alert.setHeaderText(exception.getClass().getSimpleName());
        alert.setContentText(exception.getLocalizedMessage());

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String exceptionText = sw.toString();

        Label label = new Label("Stacktrace:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.show();
    }
}

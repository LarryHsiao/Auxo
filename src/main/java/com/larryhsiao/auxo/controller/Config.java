package com.larryhsiao.auxo.controller;

import com.jfoenix.controls.JFXToggleButton;
import com.larryhsiao.auxo.config.ConfigFileSource;
import com.larryhsiao.auxo.config.ConfigPropertiesSource;
import com.larryhsiao.auxo.config.SetupWorkspace;
import com.larryhsiao.auxo.config.Workspace;
import com.larryhsiao.auxo.utils.dialogs.ExceptionAlert;
import com.larryhsiao.juno.TagDbConn;
import com.silverhetch.clotho.log.Log;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.ResourceBundle;
import java.util.function.Function;

import static javafx.scene.control.Alert.AlertType.INFORMATION;

/**
 * Controller of config page
 */
public class Config implements Initializable {
    private final Log log;
    private final File root;
    @FXML private VBox rootView;

    public Config(Log log, File root) {
        this.log = log;
        this.root = root;
    }

    @Override
    public void initialize(URL location, ResourceBundle res) {
        rootView.getChildren().add(workspace(res));
    }

    private Node workspace(ResourceBundle res) {
        final HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        var field = new TextField();
        field.setEditable(false);
        field.setText(new Workspace(new ConfigPropertiesSource(new ConfigFileSource()),
            FileSystems.getDefault().getPath(".").toFile().getAbsolutePath()
        ).value());
        item.getChildren().add(field);
        HBox.setHgrow(field, Priority.ALWAYS);

        var changeBtn = new Button("..");
        changeBtn.setOnAction(event -> {
            try {
                var fileChooser = new DirectoryChooser();
                fileChooser.setTitle(res.getString("choose_workspace"));
                var file = fileChooser.showDialog(rootView.getScene().getWindow());
                if (file == null) {
                    return;
                }
                var properties = new ConfigPropertiesSource(new ConfigFileSource());
                new TagDbConn(file).value().close();
                new SetupWorkspace(file.getAbsolutePath(), properties, new ConfigFileSource()).fire();
                log.debug("Change workspace: " + file.getAbsolutePath());
                var alert = new Alert(INFORMATION);
                alert.setTitle(res.getString("restart"));
                alert.setContentText(res.getString("restart_for_reload"));
                alert.showAndWait();
                System.exit(0);
            } catch (Exception e) {
                new ExceptionAlert(e, res).fire();
            }
        });
        item.getChildren().add(changeBtn);
        return item;
    }

    private Node toggleItem(String title, EventHandler<ActionEvent> event) {
        final HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        var label = new Label(title);
        item.getChildren().add(label);
        var toggle = new JFXToggleButton();
        toggle.setOnAction(event);
        item.getChildren().add(toggle);
        return item;
    }
}

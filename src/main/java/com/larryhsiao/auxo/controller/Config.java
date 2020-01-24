package com.larryhsiao.auxo.controller;

import com.jfoenix.controls.JFXToggleButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller of config page
 */
public class Config implements Initializable {
    @FXML
    private VBox root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        root.getChildren().add(buildConfigItem(
//            resources.getString("local_server"),
//            event -> {
//
//            }
//        ));
    }

    private Node buildConfigItem(String title, EventHandler<ActionEvent> event) {
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

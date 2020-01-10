package com.larryhsiao.auxo.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for About page
 */
public class About implements Initializable {
    @FXML private VBox infoVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        infoVBox.getChildren().add(fileTypeAttribution());
    }

    private Node fileTypeAttribution() {
        var link = new Hyperlink();
        link.setText("Icon made by prettycons from www.flaticon.com");
        link.setOnAction(event -> {
            new Thread(() -> {
                try {
                    Desktop.getDesktop()
                        .browse(new URL(
                            "https://www.flaticon.com/authors/prettycons")
                            .toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
        return link;
    }
}

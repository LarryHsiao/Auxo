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
    @FXML
    private VBox infoVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        infoVBox.getChildren().add(lines(
            "Icon made by prettycons from www.flaticon.com",
            "https://www.flaticon.com/authors/prettycons"
        ));
        infoVBox.getChildren().add(lines(
            "Source code",
            "https://github.com/LarryHsiao/Auxo"
        ));
    }

    private Node lines(String text, String url) {
        var link = new Hyperlink();
        link.setText(text);
        link.setOnAction(event -> {
            new Thread(() -> {
                try {
                    Desktop.getDesktop()
                        .browse(new URL(
                            url)
                            .toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
        return link;
    }
}

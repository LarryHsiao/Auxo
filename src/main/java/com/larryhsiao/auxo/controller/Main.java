package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for entry page of Auxo.
 */
public class Main implements Initializable {
    private static final int PAGE_TAG_MANAGEMENT = 1;
    private static final int PAGE_FILE_MANAGEMENT = 2;

    private int currentPage = -1;
    @FXML private Button tagManagement;
    @FXML private Button fileManagement;
    @FXML private AnchorPane content;

    @Override
    public void initialize(URL location, ResourceBundle res) {
        tagManagement.setText(res.getString("tag_management"));
        tagManagement.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadTagManagement(res);
            }
        });
        fileManagement.setText(res.getString("file_management"));
        fileManagement.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadFileList(res);
            }
        });
        loadFileList(res);
    }

    private void loadFileList(ResourceBundle res) {
        try {
            if (currentPage == PAGE_FILE_MANAGEMENT){
                return;
            }
            currentPage = PAGE_FILE_MANAGEMENT;
            content.getChildren().clear();
            content.getChildren().add(FXMLLoader.load(
                getClass().getResource("/com/larryhsiao/auxo/file_list.fxml"),
                res
            ));
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    private void loadTagManagement(ResourceBundle res) {
        try {
            if (currentPage == PAGE_TAG_MANAGEMENT){
                return;
            }
            currentPage = PAGE_TAG_MANAGEMENT;
            content.getChildren().clear();
            content.getChildren().add(FXMLLoader.load(
                getClass().getResource("/com/larryhsiao/auxo/tags.fxml"),
                res
            ));
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }
}

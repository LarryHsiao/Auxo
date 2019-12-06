package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.tagging.*;
import com.larryhsiao.auxo.views.TagListCell;
import com.silverhetch.clotho.Source;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for tag list page.
 */
public class TagList implements Initializable {
    private final Source<Connection> tag = new TagDbConn();
    private final ObservableList<Tag> data = FXCollections.observableArrayList();
    @FXML private TextField newTagInput;
    @FXML private ListView<Tag> tagList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        newTagInput.setOnAction(event -> {
            tagList.getItems().add(
                new CreatedTag(tag, newTagInput.getText()).value()
            );
            newTagInput.setText("");
        });
        data.addAll(new QueriedTags(new AllTags(tag)).value().values());
        tagList.setCellFactory(param -> new TagListCell());
        tagList.setItems(data);
        tagList.setContextMenu(contextMenu(resources));
    }

    private ContextMenu contextMenu(ResourceBundle resources) {
        ContextMenu menu = new ContextMenu();
        MenuItem delete = new MenuItem();
        delete.setText(resources.getString("delete"));
        delete.setOnAction(event -> {
            final Stage current = ((Stage) tagList.getScene().getWindow());
            final Tag selected = tagList.getSelectionModel().getSelectedItem();
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(selected.name());
            alert.setContentText(MessageFormat.format(resources.getString("delete_obj"), selected.name()));
            alert.setHeaderText(resources.getString("are_you_sure"));
            alert.setX(current.getX() + 150);
            alert.setY(current.getY() + 150);
            final Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                new TagDeletionById(tag, selected.id()).fire();
                tagList.getItems().remove(selected);
            }
        });
        menu.getItems().add(delete);
        return menu;
    }
}

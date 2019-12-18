package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.tagging.*;
import com.larryhsiao.auxo.views.TagListCell;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.utility.comparator.StringComparator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Comparator;
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
    @FXML private AnchorPane files;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        newTagInput.setOnAction(event -> {
            tagList.getItems().add(
                new CreatedTag(tag, newTagInput.getText()).value()
            );
            newTagInput.setText("");
        });
        data.addAll(new QueriedTags(new AllTags(tag)).value().values());
        data.sorted(new Comparator<Tag>() {
            @Override
            public int compare(Tag tag, Tag t1) {
                return new StringComparator().compare(tag.name(), t1.name());
            }
        });
        tagList.setCellFactory(param -> new TagListCell());
        tagList.setItems(data);
        tagList.setContextMenu(contextMenu(resources));
        tagList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tag>() {
            @Override
            public void changed(ObservableValue<? extends Tag> observable, Tag oldValue, Tag newValue) {
                loadTagFiles(newValue, resources);
            }
        });
    }

    private void loadTagFiles(Tag tag, ResourceBundle resources) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/larryhsiao/auxo/tag_files.fxml"), resources);
            loader.setController(new TagFiles(tag.id()));
            files.getChildren().clear();
            files.getChildren().add(loader.load());
        } catch (IOException e) {
            new ExceptionAlert(e, resources).fire();
        }
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

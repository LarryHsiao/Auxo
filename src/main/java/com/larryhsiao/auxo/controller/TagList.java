package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.views.TagListCell;
import com.larryhsiao.juno.*;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.utility.comparator.StringComparator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for tag list page.
 */
public class TagList implements Initializable {
    private final File root;
    private final Source<Connection> db;
    private final ObservableList<Tag> data = FXCollections.observableArrayList();
    @FXML private TextField newTagInput;
    @FXML private ListView<Tag> tagList;
    @FXML private AnchorPane files;

    public TagList(File root, Source<Connection> db) {
        this.root = root;
        this.db = db;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        newTagInput.setOnAction(event -> {
            tagList.getItems().add(
                new CreatedTag(db, newTagInput.getText()).value()
            );
            newTagInput.setText("");
        });
        data.addAll(new QueriedTags(new AllTags(db)).value().values());
        final StringComparator comparator = new StringComparator();
        data.sorted((tag, t1) -> comparator.compare(tag.name(), t1.name()));
        tagList.setCellFactory(param -> new TagListCell());
        tagList.setItems(data);
        tagList.setContextMenu(contextMenu(resources));
        tagList.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) ->
                loadTagFiles(newValue, resources));
    }

    private void loadTagFiles(Tag tag, ResourceBundle resources) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/larryhsiao/auxo/tag_files.fxml"), resources);
            loader.setController(new TagFiles(root, db, tag.id()));
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
                new TagDeletionById(db, selected.id()).fire();
                tagList.getItems().remove(selected);
            }
        });
        menu.getItems().add(delete);
        return menu;
    }
}

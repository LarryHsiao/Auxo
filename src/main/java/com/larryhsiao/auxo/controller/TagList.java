package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.larryhsiao.auxo.utils.SingleMediaPlayer;
import com.larryhsiao.auxo.views.TagListCell;
import com.larryhsiao.juno.*;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.log.Log;
import com.silverhetch.clotho.utility.comparator.StringComparator;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.SelectionMode.MULTIPLE;

/**
 * Controller for tag list page.
 */
public class TagList implements Initializable {
    private static final String MENU_ID_MERGE = "MENU_ID_MERGE";
    private final Log log;
    private final OkHttpClient client;
    private final File root;
    private final Source<Connection> db;
    private final ObservableList<Tag> data = observableArrayList();
    @FXML private TextField newTagInput;
    @FXML private ListView<Tag> tagList;
    @FXML private AnchorPane files;

    public TagList(Log log, OkHttpClient client, File root, Source<Connection> db) {
        this.log = log;
        this.client = client;
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
        data.addAll(new QueriedTags(new AllTags(db)).value().values()
            .stream().sorted((o1, o2) -> new StringComparator()
                .compare(o2.name(), o1.name()))
            .collect(Collectors.toList()));
        final StringComparator comparator = new StringComparator();
        data.sorted((tag, t1) -> comparator.compare(tag.name(), t1.name()));
        tagList.setCellFactory(param -> new TagListCell());
        tagList.setItems(data);
        ContextMenu menu = new ContextMenu();
        // fix not showing menu first time right click after launched.
        menu.getItems().add(deleteContext(resources));
        tagList.setContextMenu(menu);
        tagList.setOnContextMenuRequested(event -> {
            final boolean multi =
                tagList.getSelectionModel().getSelectedItems().size() > 1;
            List<MenuItem> items = tagList.getContextMenu().getItems();
            items.clear();
            if (multi) {
                items.add(mergeContext(resources));
            } else {
                items.add(renameContext(resources));
            }
            items.add(deleteContext(resources));
        });
        tagList.getSelectionModel().getSelectedItems().addListener(
            (ListChangeListener<Tag>) c -> {
                loadTagFiles(
                    tagList.getSelectionModel().getSelectedItems(),
                    resources
                );
            });
        tagList.getSelectionModel().setSelectionMode(MULTIPLE);
    }

    private void loadTagFiles(List<Tag> tag, ResourceBundle resources) {
        try {
            SingleMediaPlayer.release();
            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/larryhsiao/auxo/tag_files.fxml"),
                resources
            );
            loader.setController(
                new TagFiles(log, client ,root,
                    db,
                    tag.stream().mapToLong(Tag::id).toArray())
            );
            files.getChildren().clear();
            files.getChildren().add(loader.load());
        } catch (IOException e) {
            new ExceptionAlert(e, resources).fire();
        }
    }

    private MenuItem mergeContext(ResourceBundle res) {
        final MenuItem merge = new MenuItem();
        merge.setId(MENU_ID_MERGE);
        merge.setText(res.getString("merge"));
        merge.setOnAction(event -> {
            final var selected = tagList.getSelectionModel().getSelectedItems();
            new TagMerging(db, selected.toArray(new Tag[0])).fire();
            tagList.getItems().removeAll(selected.subList(1, selected.size()));
        });
        return merge;
    }

    private MenuItem renameContext(ResourceBundle res) {
        MenuItem rename = new MenuItem();
        rename.setText(res.getString("rename"));
        rename.setOnAction(event -> {
            final Tag selected = tagList.getSelectionModel().getSelectedItem();
            final TextInputDialog dialog =
                new TextInputDialog(selected.name());
            dialog.setTitle(selected.name());
            dialog.setHeaderText(res.getString("rename"));
            dialog.setContentText(res.getString("new_name"));
            final Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                final int idx = tagList.getSelectionModel().getSelectedIndex();
                tagList.getItems().add(
                    idx + 1,
                    new RenamedTag(db, selected, newName).value()
                );
                tagList.getItems().remove(idx);
            });
        });
        return rename;
    }

    private MenuItem deleteContext(ResourceBundle res) {
        MenuItem delete = new MenuItem();
        delete.setText(res.getString("delete"));
        delete.setOnAction(event -> {
            final List<Tag> selected = tagList.getSelectionModel()
                .getSelectedItems();
            if (selected.size() == 1) {
                delete(selected.get(0), res);
            } else {
                delete(selected, res);
            }
        });
        return delete;
    }

    private void delete(Tag selected, ResourceBundle res) {
        final Stage current = ((Stage) tagList.getScene().getWindow());
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(selected.name());
        alert.setContentText(MessageFormat.format(
            res.getString("delete_obj"),
            selected.name())
        );
        alert.setHeaderText(res.getString("are_you_sure"));
        alert.setX(current.getX() + 150);
        alert.setY(current.getY() + 150);
        final Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new TagDeletionById(db, selected.id()).fire();
            tagList.getItems().remove(selected);
        }
    }

    private void delete(List<Tag> selected, ResourceBundle res) {
        final Stage current = ((Stage) tagList.getScene().getWindow());
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(res.getString("delete_tags"));
        alert.setContentText(MessageFormat.format(
            res.getString("are_you_sure_to_delete_tags"),
            selected.size())
        );
        alert.setHeaderText(res.getString("delete"));
        alert.setX(current.getX() + 150);
        alert.setY(current.getY() + 150);
        final Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new TagDeletionById(
                db,
                selected.stream().mapToLong(Tag::id).toArray()
            ).fire();
            tagList.getItems().removeAll(selected);
        }
    }
}

package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.utils.AuxoExecute;
import com.larryhsiao.auxo.utils.FileComparator;
import com.larryhsiao.auxo.views.FileListCell;
import com.larryhsiao.juno.DetachAction;
import com.larryhsiao.juno.FileByName;
import com.larryhsiao.juno.FilesByTagId;
import com.larryhsiao.juno.QueriedAFiles;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.utility.comparator.StringComparator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.MouseButton.PRIMARY;

/**
 * Controller for file list that have given tag attached.
 */
public class TagFiles implements Initializable {
    private final File root;
    private final Source<Connection> db;
    private final long[] tagId;
    @FXML private ListView<File> fileList;

    public TagFiles(File root, Source<Connection> db, long... tagId) {
        this.root = root;
        this.db = db;
        this.tagId = tagId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileList.setCellFactory(param -> new FileListCell());
        fileList.getItems().addAll(
            new QueriedAFiles(
                new FilesByTagId(db, tagId)
            ).value().values().stream()
             .map(aFile -> new File(
                 root,
                 aFile.name()
             ))
             .sorted(new FileComparator((o1, o2) -> new StringComparator()
                 .compare(o2.getName(), o1.getName())))
             .collect(Collectors.toList())
        );

        fileList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                new AuxoExecute(
                    root, ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        fileList.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                new AuxoExecute(
                    root, ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        final var menu =new ContextMenu();
        menu.getItems().add(removeContext(resources));
        fileList.setContextMenu(menu);
        fileList.setOnContextMenuRequested(event -> {
            fileList.getContextMenu().getItems().clear();
            if (tagId.length == 1) {
                fileList.getContextMenu()
                        .getItems().add(removeContext(resources));
            }
        });
    }

    private MenuItem removeContext(ResourceBundle res) {
        final MenuItem remove = new MenuItem(res.getString("delete"));
        remove.setOnAction(event -> {
            final File selected =
                fileList.getSelectionModel().getSelectedItem();
            new DetachAction(
                db,
                new FileByName(db, selected.getName()).value().id(),
                tagId[0]
            ).fire();
            fileList.getItems().remove(selected);
        });
        return remove;
    }
}

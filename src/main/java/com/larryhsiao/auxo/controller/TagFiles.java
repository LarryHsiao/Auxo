package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.utils.AuxoExecute;
import com.larryhsiao.auxo.utils.FileComparator;
import com.larryhsiao.auxo.utils.FileMimeType;
import com.larryhsiao.auxo.utils.SingleMediaPlayer;
import com.larryhsiao.auxo.utils.views.FileListCell;
import com.larryhsiao.auxo.workspace.FsFiles;
import com.larryhsiao.juno.DetachAction;
import com.larryhsiao.juno.FileByName;
import com.larryhsiao.juno.FilesByTagId;
import com.larryhsiao.juno.QueriedAFiles;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.log.Log;
import com.silverhetch.clotho.utility.comparator.StringComparator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.MouseButton.PRIMARY;

/**
 * Controller for file list that have given tag attached.
 */
public class TagFiles implements Initializable {
    private final Log log;
    private final OkHttpClient client;
    private final File root;
    private final Source<Connection> db;
    private final long[] tagId;
    @FXML private ListView<File> fileList;
    @FXML private AnchorPane contents;

    public TagFiles(
        Log log, OkHttpClient client, File root, Source<Connection> db,
        long... tagId) {
        this.log = log;
        this.client = client;
        this.root = root;
        this.db = db;
        this.tagId = tagId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileList.setCellFactory(param -> new FileListCell());
        Map<String, File> value = new FsFiles(root).value();
        fileList.getItems().addAll(
            new QueriedAFiles(
                new FilesByTagId(db, tagId)
            ).value().values().stream()
                .filter(aFile -> value.containsKey(aFile.name()))
                .map(aFile -> new File(
                    root,
                    aFile.name()
                ))
                .sorted(new FileComparator((o1, o2) ->
                    new StringComparator().compare(o2.getName(), o1.getName()))
                ).collect(Collectors.toList())
        );

        fileList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                new AuxoExecute(
                    client, log, root, ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        fileList.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                new AuxoExecute(
                    client, log, root, ((Stage) fileList.getScene().getWindow()),
                    fileList.getSelectionModel().getSelectedItem(),
                    resources
                ).fire();
            }
        });
        final var menu = new ContextMenu();
        menu.getItems().add(removeContext(resources));
        fileList.setContextMenu(menu);
        fileList.setOnContextMenuRequested(event -> {
            fileList.getContextMenu().getItems().clear();
            if (tagId.length == 1) {
                fileList.getContextMenu()
                    .getItems().add(removeContext(resources));
            }
        });

        fileList.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                try {
                    SingleMediaPlayer.release();
                    final var mimeType = new FileMimeType(newValue).value();
                    if (mimeType.startsWith("image")) {
                        loadImage(newValue, resources);
                    } else if (mimeType.startsWith("video")) {
                        loadMedia(newValue, resources);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private void loadMedia(File fsFile, ResourceBundle res) throws IOException {
        final FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/larryhsiao/auxo/video_player.fxml"),
            res
        );
        loader.setController(new Player(fsFile.toURI().toASCIIString()));
        final Parent rootView = loader.load();
        contents.getChildren().clear();
        contents.getChildren().add(rootView);
    }

    private void loadImage(File fsFile, ResourceBundle res) {
        final ImageView imageView = new ImageView(
            new Image(fsFile.toURI().toASCIIString(), true)
        );
        imageView.setPreserveRatio(true);
        contents.getChildren().clear();
        contents.getChildren().add(imageView);
        imageView.fitHeightProperty().bind(contents.heightProperty());
        imageView.fitWidthProperty().bind(contents.widthProperty());
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

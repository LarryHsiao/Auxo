package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.tagging.AllTags;
import com.larryhsiao.auxo.tagging.FakeDataConn;
import com.larryhsiao.auxo.tagging.QueriedTags;
import com.larryhsiao.auxo.tagging.TagDbConn;
import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for tag list page.
 */
public class TagList implements Initializable {
    private final ObservableList<String> data = FXCollections.observableArrayList();
    @FXML
    private ListView<String> tagList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        data.addAll(new QueriedTags(
            new AllTags(
                new FakeDataConn(
                    new TagDbConn(
                        new InMemoryConn()
                    )
                )
            )
        ).value().keySet());
        tagList.setItems(data);
    }
}

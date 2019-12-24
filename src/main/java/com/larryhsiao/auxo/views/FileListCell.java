package com.larryhsiao.auxo.views;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * List cell that display a file.
 */
public class FileListCell extends ListCell<File> {
    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText("");
            setGraphic(null);
        } else {
            setText(item.getName());
            loadImage(item);
        }
    }

    private void loadImage(File item) {
        try {
            final String imageUrl;
            if ("image/png".equals(Files.probeContentType(item.toPath())) ||
                "image/jpeg".equals(Files.probeContentType(item.toPath()))) {
                imageUrl = item.toURI().toASCIIString();
            } else if ("..".equals(item.getName())) {
                imageUrl = getClass().getResource("/images/back.png").toString();
            } else if (item.isDirectory()) {
                imageUrl = getClass().getResource("/images/dir.png").toString();
            } else {
                imageUrl = getClass().getResource("/images/file.png").toString();
            }
            final StackPane container = new StackPane();
            container.setPrefSize(75, 75);
            final ImageView imageView = new ImageView(
                new Image(
                    imageUrl,
                    75,
                    75,
                    true,
                    true,
                    true)
            );
            imageView.setPreserveRatio(true);
            imageView.prefWidth(75);
            imageView.prefHeight(75);
            container.getChildren().add(imageView);
            StackPane.setAlignment(imageView, Pos.CENTER);
            setGraphic(container);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

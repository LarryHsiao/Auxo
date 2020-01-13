package com.larryhsiao.auxo.views;

import com.silverhetch.clotho.file.Extension;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static javafx.scene.input.TransferMode.MOVE;

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
            var image = image(item);
            final StackPane container = new StackPane();
            container.setPrefSize(75, 75);
            final ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.prefWidth(75);
            imageView.prefHeight(75);
            container.getChildren().add(imageView);
            StackPane.setAlignment(imageView, Pos.CENTER);
            setGraphic(container);
            setOnDragDetected(event -> {
                final var board = startDragAndDrop(MOVE);
                final var content = new ClipboardContent();
                content.putFiles(Collections.singletonList(item));
                board.setContent(content);
                board.setDragView(imageView.getImage());
                event.consume();
            });
        }
    }

    private Image image(File item) {
        try {
            final String imageUrl;
            if ("image/png".equals(Files.probeContentType(item.toPath())) ||
                "image/jpeg".equals(Files.probeContentType(item.toPath()))) {
                imageUrl = item.toURI().toASCIIString();
            } else if ("..".equals(item.getName())) {
                imageUrl =
                    getClass().getResource("/images/back.png").toString();
            } else if (item.isDirectory()) {
                imageUrl = getClass().getResource("/images/dir.png").toString();
            } else {
                imageUrl = new FileIconUrl(new Extension(item)).value();
            }
            return new Image(
                imageUrl,
                75,
                75,
                true,
                true,
                true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

package com.larryhsiao.auxo.utils.views;

import com.larryhsiao.auxo.utils.FileTypeDetector;
import com.silverhetch.clotho.file.Extension;
import com.silverhetch.clotho.file.FileSize;
import com.silverhetch.clotho.file.SizeText;
import com.silverhetch.clotho.log.Log;
import com.silverhetch.clotho.log.PhantomLog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.takes.http.Back;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.scene.control.ContentDisplay.RIGHT;
import static javafx.scene.input.TransferMode.MOVE;
import static javafx.scene.layout.Priority.ALWAYS;

/**
 * List cell that display a file.
 */
public class FileListCell extends ListCell<File> {
    private final Log log;

    public FileListCell(Log log) {
        this.log = log;
    }

    public FileListCell() {
        this(new PhantomLog());
    }

    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            var image = image(item);
            final HBox container = new HBox();
            container.prefWidth(700);
            container.setAlignment(CENTER_LEFT);
            final ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.prefWidth(75);
            imageView.prefHeight(75);
            container.getChildren().add(imageView);

            var nameLabel = new Label(item.getName());
            nameLabel.setMaxWidth(550);
            container.getChildren().add(nameLabel);

            var sizePane = new HBox();
            sizePane.setAlignment(Pos.CENTER_RIGHT);
            var sizeLabel = new Label(new SizeText(new FileSize(item.toPath())).value());
            sizePane.getChildren().add(sizeLabel);
            container.getChildren().add(sizePane);
            HBox.setHgrow(sizePane, ALWAYS);

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
            final String contentType = new FileTypeDetector().probeContentType(item.toPath());
            log.debug(item.toURI().toString() + " ContentType: " + contentType);
            final String imageUrl;
            if (contentType != null && contentType.startsWith("image")) {
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

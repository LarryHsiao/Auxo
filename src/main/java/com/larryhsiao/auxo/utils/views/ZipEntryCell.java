package com.larryhsiao.auxo.utils.views;

import com.larryhsiao.auxo.utils.FileTypeDetector;
import com.silverhetch.clotho.file.Extension;
import com.silverhetch.clotho.log.Log;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static javafx.scene.input.TransferMode.MOVE;

/**
 * ListCell to showing an zip entry..
 */
public class ZipEntryCell extends ListCell<String> {
    private final Log log;
    private final ZipFile file;

    public ZipEntryCell(Log log, ZipFile file) {
        this.log = log;
        this.file = file;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText("");
            setGraphic(null);
        } else {
            setText(item);
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
        }
    }

    private Image image(String name) {
        try {
            if (name.equals("..")) {
                return new Image(
                    getClass().getResource("/images/back.png").toExternalForm(),
                    75, 75,
                    true,
                    true,
                    true
                );
            }
            final var item = file.getEntry(name);
            final String contentType = new FileTypeDetector().probeContentType(Path.of(item.getName()));
            log.debug(item.getName() + " ContentType: " + contentType);
            if (contentType != null && contentType.startsWith("image")) {
                return new Image(
                    file.getInputStream(item),
                    75,
                    75,
                    true,
                    true);
            } else if (item.isDirectory()) {
                return new Image(
                    getClass().getResource("/images/dir.png").toExternalForm(),
                    75,
                    75,
                    true,
                    true,
                    true);
            } else {
                return new Image(
                    new FileIconUrl(new Extension(new File(item.getName()))).value(),
                    75,
                    75,
                    true,
                    true,
                    true);
            }
//            } else if ("..".equals(item.getName())) {
//                imageUrl =
//                    getClass().getResource("/images/back.png").toString();
//            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

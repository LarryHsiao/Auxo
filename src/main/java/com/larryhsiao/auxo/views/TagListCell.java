package com.larryhsiao.auxo.views;

import com.larryhsiao.auxo.tagging.Tag;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * List cell for {@link Tag}.
 */
public class TagListCell extends ListCell<Tag> {
    @Override
    protected void updateItem(Tag item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText("");
            setGraphic(null);
        } else {
            setText(item.name());
            setGraphic(new ImageView(new Image(
                getClass().getResource("/images/tag.png").toString()
            )));
        }
    }
}

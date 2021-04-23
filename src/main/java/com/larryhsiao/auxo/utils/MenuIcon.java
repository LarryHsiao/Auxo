package com.larryhsiao.auxo.utils;

import com.larryhsiao.clotho.Source;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * Source to build a ImageView for context menu
 */
public class MenuIcon implements Source<ImageView> {
    private final String path;

    public MenuIcon(String path) {
        this.path = path;
    }

    @Override
    public ImageView value() {
        return new ImageView(new Image(
            getClass().getResource(path).toExternalForm(),
            48.0, 48.0, true, true, true
        ));
    }
}

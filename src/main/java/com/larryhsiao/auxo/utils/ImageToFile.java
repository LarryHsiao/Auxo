package com.larryhsiao.auxo.utils;

import com.silverhetch.clotho.Action;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Action to save a given Image(javafx) to file
 */
public class ImageToFile implements Action {
    private final File target;
    private final Image image;

    public ImageToFile(File target, Image image) {
        this.target = target;
        this.image = image;
    }

    @Override
    public void fire() {
        try {
            BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            ImageIO.write(bImage, "png", target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.larryhsiao.auxo.utils;

import com.silverhetch.clotho.Action;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Action to launch file by default application.
 */
public class PlatformExecute implements Action {
    private final File file;

    public PlatformExecute(File file) {
        this.file = file;
    }

    @Override
    public void fire() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException("Can't open file " + file.getAbsolutePath());
        }
    }
}

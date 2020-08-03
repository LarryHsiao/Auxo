package com.larryhsiao.auxo.utils;

import com.silverhetch.clotho.Action;

import java.awt.*;
import java.io.File;

/**
 * Action to highlight given file in File browser.
 */
public class ShownInBrowser implements Action {
    private final File file;

    public ShownInBrowser(File file) {
        this.file = file;
    }

    @Override
    public void fire() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                new ProcessBuilder(
                    "explorer.exe",
                    "/select,"+ file.toURI().toString()
                ).start();
            } else {
                Desktop.getDesktop().browseFileDirectory(file);
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException("Can't show file " + file.getAbsolutePath());
        }
    }
}

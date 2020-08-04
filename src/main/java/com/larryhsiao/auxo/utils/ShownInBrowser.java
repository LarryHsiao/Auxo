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
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("windows")) {
                new ProcessBuilder(
                    "explorer.exe",
                    "/select," + file.toURI().toString()
                ).start();
            } else if (osName.contains("linux")) {
                if (new File("/usr/bin/dolphin").exists()){
                    new ProcessBuilder(
                        "/usr/bin/dolphin",
                        "--select", file.getAbsolutePath()
                    ).start();
                }else {
                    throw new RuntimeException();
                }
            } else {
                Desktop.getDesktop().browseFileDirectory(file);
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException("Can't show file " + file.getAbsolutePath(), e);
        }
    }
}

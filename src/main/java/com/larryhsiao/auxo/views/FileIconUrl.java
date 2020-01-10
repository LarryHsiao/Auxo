package com.larryhsiao.auxo.views;

import com.silverhetch.clotho.Source;

import java.io.File;
import java.net.URL;

/**
 * Source to build icon url string from given extension.
 */
public class FileIconUrl implements Source<String> {
    private final Source<String> extension;

    public FileIconUrl(Source<String> extension) {
        this.extension = extension;
    }

    @Override
    public String value() {
        try {
            URL iconFileUrl = getClass()
                .getResource("/images/file_type/" + extension.value() + ".png");
            if (new File(iconFileUrl.toURI()).exists()) {
                return iconFileUrl.toString();
            } else {
                return getClass()
                    .getResource("/images/file_type/046-file-45.png")
                    .toString();
            }
        } catch (Exception e) {
            return getClass()
                .getResource("/images/file_type/file.png")
                .toString();
        }
    }
}

package com.larryhsiao.auxo.utils.views;

import com.silverhetch.clotho.Source;

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
            if (iconFileUrl != null) {
                return iconFileUrl.toString();
            } else {
                return getClass()
                    .getResource("/images/file_type/file.png")
                    .toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getClass()
                .getResource("/images/file_type/file.png")
                .toString();
        }
    }
}

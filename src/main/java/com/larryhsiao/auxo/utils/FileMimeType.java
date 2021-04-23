package com.larryhsiao.auxo.utils;

import com.larryhsiao.clotho.Source;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Source to fetch mimeType of given file.
 * <p>
 * This class is just wrap the JDK api to Not return null.
 */
public class FileMimeType implements Source<String> {
    private final File file;

    public FileMimeType(File file) {
        this.file = file;
    }

    @Override
    public String value() {
        try {
            final String mimeType = new FileTypeDetector().probeContentType(file.toPath());
            if (mimeType == null || mimeType.isEmpty()) {
                return "application/octet-stream";
            }
            return mimeType;
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}

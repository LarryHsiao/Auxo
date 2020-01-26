package com.larryhsiao.auxo.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File detector for determine the file's content type.
 */
public class FileTypeDetector extends java.nio.file.spi.FileTypeDetector {
    @Override
    public String probeContentType(Path path) throws IOException {
        var jdkFileType = Files.probeContentType(path);
        if (jdkFileType != null) {
            return jdkFileType;
        } else {
            var fileName = path.toFile().getName();
            if (fileName.endsWith(".md")) {
                return "text/markdown";
            }
            return null;
        }
    }
}

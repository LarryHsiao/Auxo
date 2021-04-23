package com.larryhsiao.auxo.config;

import com.larryhsiao.clotho.Source;

import java.io.File;
import java.util.Properties;

/**
 * Source to build workspace
 */
public class Workspace implements Source<String> {
    private final Source<Properties> source;
    private final String defaultPath;

    public Workspace(Source<Properties> source, String defaultPath) {
        this.source = source;
        this.defaultPath = defaultPath;
    }

    @Override
    public String value() {
        var path = source.value().getProperty("workspace");
        if (path == null || !new File(path).exists()) {
            return defaultPath;
        }
        return path;
    }
}

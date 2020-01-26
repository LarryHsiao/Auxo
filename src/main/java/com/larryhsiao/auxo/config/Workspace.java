package com.larryhsiao.auxo.config;

import com.silverhetch.clotho.Source;

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
        if (path == null) {
            return defaultPath;
        }
        return path;
    }
}

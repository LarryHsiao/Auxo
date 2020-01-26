package com.larryhsiao.auxo.config;

import com.silverhetch.clotho.Source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Properties;

/**
 * Source to build Jdk Properties object from given path.
 */
public class ConfigPropertiesSource implements Source<Properties> {
    private final Source<File> file;

    public ConfigPropertiesSource(Source<File> file) {
        this.file = file;
    }

    @Override
    public Properties value() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file.value()));
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return properties;
    }
}

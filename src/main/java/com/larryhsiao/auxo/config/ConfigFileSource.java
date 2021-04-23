package com.larryhsiao.auxo.config;

import com.larryhsiao.clotho.Source;

import java.io.File;
import java.io.IOException;

/**
 * Source to build config file.
 */
public class ConfigFileSource implements Source<File> {
    @Override
    public File value() {
        try {
            final File configFile = new File(System.getProperty("user.home") + File.separator + ".auxo");
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            return configFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

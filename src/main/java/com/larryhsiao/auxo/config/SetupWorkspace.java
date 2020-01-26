package com.larryhsiao.auxo.config;

import com.silverhetch.clotho.Action;
import com.silverhetch.clotho.Source;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Action to store workspace to config file.
 */
public class SetupWorkspace implements Action {
    private final String path;
    private final Source<Properties> propertiesSource;
    private final Source<File> configFile;

    public SetupWorkspace(String path, Source<Properties> propertiesSource, Source<File> configFile) {
        this.path = path;
        this.propertiesSource = propertiesSource;
        this.configFile = configFile;
    }

    @Override
    public void fire() {
        try {
            var properties = propertiesSource.value();
            properties.setProperty("workspace", path);
            properties.store(new FileOutputStream(configFile.value()), null);
        }catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

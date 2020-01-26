package com.larryhsiao.auxo.devices;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.silverhetch.clotho.Source;

import java.io.File;
import java.util.List;

/**
 * Json of given file.
 */
public class DeviceJson implements Source<String> {
    private final Source<List<File>> fileSource;

    public DeviceJson(Source<List<File>> fileSource) {
        this.fileSource = fileSource;
    }

    @Override
    public String value() {
        final JsonArray json = new JsonArray();
        final var files = fileSource.value();
        if (files != null) {
            for (File file : files) {
                final JsonObject obj = new JsonObject();
                obj.addProperty("name", file.getName());
                obj.addProperty("isDirectory", file.isDirectory());
                json.add(obj);
            }
        }
        return json.toString();
    }
}

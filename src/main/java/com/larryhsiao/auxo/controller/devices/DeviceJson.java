package com.larryhsiao.auxo.controller.devices;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.silverhetch.clotho.Source;

import java.io.File;

/**
 * Json of given file.
 */
public class DeviceJson implements Source<String> {
    private final File root;

    public DeviceJson(File root) {
        this.root = root;
    }

    @Override
    public String value() {
        final JsonArray json = new JsonArray();
        final File[] files = root.listFiles();
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

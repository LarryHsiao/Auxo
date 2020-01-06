package com.larryhsiao.auxo.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.takes.HttpException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.RqHeaders;
import org.takes.rq.RqHref;
import org.takes.rs.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Api for accessing given root files.
 */
public class TkFiles implements Take {
    private final File root;

    public TkFiles(File root) {
        this.root = root;
    }

    @Override
    public Response act(Request request) throws IOException {
        final File file = new File(
            root, new RqHref.Base(request).href().path()
        );
        if (!file.exists()) {
            throw new HttpException(
                HttpURLConnection.HTTP_NOT_FOUND,
                String.format(
                    "%s not found", file.getAbsolutePath()
                )
            );
        }
        if (file.isDirectory()) {
            return new RsWithType(
                new RsWithStatus(
                    new RsText(contentJson(file)),
                    HttpURLConnection.HTTP_OK
                ), "application/json"
            );
        } else {
            final InputStream is = new FileInputStream(file);
            if (new RqHeaders.Smart(request).names().contains("range")) {
                is.skip(startLocation(request));
                return new RsWithStatus(new RsWithHeader(new RsWithBody(is),
                    "range", startLocation(request) + "-"), 206);
            }
            return new RsWithBody(is);
        }
    }

    private long startLocation(Request req) throws IOException {
        return Long.parseLong(
            new RqHeaders.Smart(req).single("Range")
                .replace("bytes=", "")
                .replace("-", "")
        );
    }

    private String contentJson(File root) {
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

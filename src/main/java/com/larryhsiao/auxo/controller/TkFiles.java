package com.larryhsiao.auxo.controller;

import com.google.gson.JsonArray;
import org.takes.HttpException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.RqHref;
import org.takes.rs.RsText;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithStatus;
import org.takes.rs.RsWithType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
            return new RsWithBody(new FileInputStream(file));
        }
    }

    private String contentJson(File root) {
        JsonArray json = new JsonArray();
        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                json.add(file.getName());
            }
        }
        return json.toString();
    }
}

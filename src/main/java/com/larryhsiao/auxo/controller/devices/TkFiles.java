package com.larryhsiao.auxo.controller.devices;

import com.larryhsiao.auxo.workspace.FsFiles;
import com.larryhsiao.juno.FavoriteFiles;
import com.larryhsiao.juno.QueriedAFiles;
import com.silverhetch.clotho.Source;
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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Api for accessing given root files.
 */
public class TkFiles implements Take {
    private final Source<Connection> db;
    private final File root;

    public TkFiles(
        Source<Connection> db, File root) {
        this.db = db;
        this.root = root;
    }

    @Override
    public Response act(Request request) throws IOException {
        final var href = new RqHref.Smart(request);
        final File file = new File(
            root, href.href().path()
        );
        final boolean favorite =
            root.getCanonicalPath().equals(file.getCanonicalPath()) &&
                href.href().toString().contains("favorite=") &&
                "true".equals(href.href().param("favorite")
                    .iterator()
                    .next());
        if (!file.exists()) {
            throw new HttpException(
                HttpURLConnection.HTTP_NOT_FOUND,
                String.format("%s not found", file.getAbsolutePath()));
        }
        if (file.isDirectory()) {
            return new RsWithType(
                new RsWithStatus(
                    new RsText(new DeviceJson(() -> {
                        if (favorite) {
                            var dbFile =
                                new QueriedAFiles(new FavoriteFiles(db))
                                    .value();
                            return new FsFiles(root).value().values()
                                .stream().filter(file1 ->
                                    dbFile.containsKey(file1.getName()))
                                .collect(Collectors.toList());
                        } else {
                            return new ArrayList<>(
                                new FsFiles(file).value().values()
                            );
                        }
                    }
                    ).value()),
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
}

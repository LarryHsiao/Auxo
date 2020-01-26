package com.larryhsiao.auxo.utils;

import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.file.ToFile;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Action to download file from url.
 */
public class UrlFile implements Source<File> {
    private final File targetDir;
    private final OkHttpClient client;
    private final String url;

    public UrlFile(File targetDir, OkHttpClient client, String url) {
        this.targetDir = targetDir;
        this.client = client;
        this.url = url;
    }

    @Override
    public File value() {
        try {
            var response = client.newCall(new Request.Builder()
                .url(url)
                .build()
            ).execute();
            var header = response.header("Content-Type");
            var extension = '.' + header.substring(header.lastIndexOf('/') + 1);
            var fileName = url.substring(url.lastIndexOf('/') + 1).trim();
            var newFile = new File(targetDir, fileName + extension);
            if (newFile.exists()) {
                newFile = new File(
                    targetDir,
                    fileName + UUID
                        .randomUUID().toString().substring(0, 4) + extension
                );
            }
            new ToFile(
                response.body().byteStream(),
                newFile, integer -> null
            ).fire();
            return newFile;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

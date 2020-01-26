package com.larryhsiao.auxo.devices;

import com.silverhetch.clotho.encryption.Base64Encode;
import com.silverhetch.clotho.encryption.MD5;
import com.silverhetch.clotho.source.ConstSource;
import org.takes.HttpException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.RqHref;
import org.takes.rs.RsEmpty;
import org.takes.rs.RsWithHeaders;
import org.takes.rs.RsWithStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Api for accessing given root files.
 */
public class TkFileHeads implements Take {
    private final File root;

    public TkFileHeads(File root) {
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
            return new RsWithStatus(406);
        } else {
            return new RsWithStatus(new RsWithHeaders(
                new RsEmpty(),
                "Content-MD5: "+new Base64Encode(
                    new ConstSource<>(
                        new MD5(new FileInputStream(
                            new File(root, file.getName())
                        )).value().getBytes()
                    )).value()
            ), 204);
        }
    }
}

package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.source.ConstSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link FsAFile}
 */
class FsAFileTest {
    /**
     * Check the result should be the items that both source have.
     */
    @Test
    void resultSize() throws Exception {
        final HashMap<String, AFile> origin = new HashMap<>();
        origin.put("key1", new ConstAFile("key1"));
        origin.put("key2", new ConstAFile("key2"));
        origin.put("key3", new ConstAFile("key3"));
        origin.put("key4", new ConstAFile("key4"));
        final HashMap<String, File> intersection = new HashMap<>();
        intersection.put("key2", File.createTempFile("temp", ""));
        intersection.put("key3", File.createTempFile("temp", ""));
        intersection.put("key4", File.createTempFile("temp", ""));
        intersection.put("key5", File.createTempFile("temp", ""));

        final Map<String, AFile> result = new FsAFile(
            new ConstSource<>(origin),
            new ConstSource<>(intersection.keySet())
        ).value();
        assertEquals(
            3,
            result.size()
        );
    }
}
package com.larryhsiao.auxo.workspace;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link FsFiles}.
 */
class FsFilesTest {
    /**
     * Check the result size.
     */
    @Test
    void resultSize() throws Exception {
        File root = Files.createTempDirectory("temp-").toFile();
        for (int i = 0; i < 10; i++) {
            if (!File.createTempFile("temp", "", root).exists()) {
                throw new RuntimeException("Temp file construction failed.");
            }
        }
        assertEquals(
            10,
            new FsFiles(root).value().size()
        );
    }

    @Test
    void exceptionOnNotDirectory() {
        try {
            new FsFiles(File.createTempFile("temp", "")).value();
            fail("Should throw exception.");
        } catch (Exception e) {
            assertTrue(true);
        }
    }
}
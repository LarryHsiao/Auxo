package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Test for {@link QueriedAFiles}.
 */
class QueriedAFilesTest {

    /**
     * Check the files result is exist an valid.
     */
    @Test
    void nameExist() {
        Map<String, AFile> result = new QueriedAFiles(
            new AllFiles(
                new FakeDataConn(
                    new TagDbConn(new InMemoryConn())
                )
            )
        ).value();

        assertNotEquals(0, result.size());
        result.forEach((s, aFile) -> {
            assertNotEquals("", s);
            assertNotEquals("", aFile.name());
        });
    }
}
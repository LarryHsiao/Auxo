package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link FileById}.
 */
class FileByIdTest {
    /**
     * Check the query reuslt from fake data.
     */
    @Test
    void simple() {
        assertEquals(
            "filename2",
            new QueriedAFile(
                new FileById(
                    new FakeDataConn(
                        new TagDbConn(
                            new InMemoryConn()
                        )
                    ),
                    2
                )
            ).value().name()
        );
    }
}
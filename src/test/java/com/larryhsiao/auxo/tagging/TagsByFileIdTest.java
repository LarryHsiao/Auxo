package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TagsByFileId}.
 */
class TagsByFileIdTest {

    /**
     * Check the queried result count.
     */
    @Test
    void checkSizeWithFakeData() {
        assertEquals(
            2,
            new QueriedTags(
                new TagsByFileId(
                    new FakeDataConn(
                        new TagDbConn(new InMemoryConn())
                    ),
                    1
                )
            ).value().size()
        );
    }
}
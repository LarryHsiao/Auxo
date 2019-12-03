package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link TagByName}.
 */
class TagByNameTest {
    /**
     * Check the built Tag have same name as input.
     */
    @Test
    void existResult() {
        assertEquals(
            "tag",
            new TagByName(
                new FakeDataConn(
                    new TagDbConn(
                        new InMemoryConn()
                    )
                ),
                "tag"
            ).value().name()
        );
    }

    /**
     * Should create an entry for non-exist tag.
     */
    @Test
    void nonExistResult() {
        assertEquals(
            "tag_non_exist",
            new TagByName(
                new FakeDataConn(
                    new TagDbConn(
                        new InMemoryConn()
                    )
                ),
                "tag_non_exist"
            ).value().name()
        );
    }
}
package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.SingleConn;
import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

/**
 * Test for {@link TagDeletionById}
 */
class TagDeletionByIdTest {

    /**
     * Check delete tag is not exist.
     */
    @Test
    void simple() {
        Source<Connection> conn = new SingleConn(new FakeDataConn(new TagDbConn(new InMemoryConn())));
        new TagDeletionById(conn, 2).fire();
        Assertions.assertEquals(1, new QueriedTags(new AllTags(conn)).value().size());
        Assertions.assertEquals(1, new QueriedTags(new TagsByFileId(conn, 1)).value().size());
    }
}
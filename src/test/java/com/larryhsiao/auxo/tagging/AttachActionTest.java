package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import com.silverhetch.clotho.source.ConstSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

/**
 * Test for {@link AttachAction}.
 */
class AttachActionTest {

    /**
     * Check the attach process.
     */
    @Test
    void checkNewTagIsAttached() {
        Source<Connection> db = new ConstSource<>(
            new FakeDataConn(
                new TagDbConn(new InMemoryConn())
            ).value()
        );
        new AttachAction(
            db,
            2,
            new CreatedTag(db, "Random").value().id()
        ).fire();

        Assertions.assertEquals(
            2,
            new QueriedTags(
                new TagsByFileId(db, 2)
            ).value().size()
        );
    }
}
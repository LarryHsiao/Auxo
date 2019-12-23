package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.SingleConn;
import com.silverhetch.clotho.database.sqlite.InMemoryConn;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link FilesNotTagged}
 */
class FilesNotTaggedTest {
    /**
     * Check the size.
     */
    @Test
    void entryCount() throws SQLException {
        final Source<Connection> db = new SingleConn(new FakeDataConn(
            new TagDbConn(new InMemoryConn())
        ));
        db.value().prepareStatement(
            //language=SQLite
            "INSERT INTO files(name) VALUES ('temp_file_name');"
        ).execute();
        assertEquals(
            1,
            new QueriedAFiles(
                new FilesNotTagged(
                    db
                )
            ).value().size()
        );
    }
}
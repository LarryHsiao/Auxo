package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.source.ConstSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link AllFiles}.
 */
class AllFilesTest {
    /**
     * Check the field exists.
     */
    @Test
    void fields() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            ResultSet result = new AllFiles(new FakeDataConn(new TagDbConn(new ConstSource<>(conn)))).value();
            result.next();
            assertNotEquals(
                "",
                result.getString("name")
            );
        }
    }

    /**
     * Check that it should have a exception if table not constructed.
     */
    @Test
    void exceptionNoTable() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            new AllFiles(new ConstSource<>(conn)).value();
            fail("Should throw a exception.");
        } catch (Exception e) {
            assertTrue(true);
        }
    }
}
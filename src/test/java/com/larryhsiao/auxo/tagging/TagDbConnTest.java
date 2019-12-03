package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.source.ConstSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for {@link TagDbConn}
 */
class TagDbConnTest {

    /**
     * Check if the database available
     */
    @Test
    void isValid() {
        try {
            assertTrue(
                new TagDbConn(
                    new ConstSource<>(
                        DriverManager.getConnection("jdbc:sqlite::memory:")
                    )
                ).value().isValid(0)
            );
        } catch (SQLException e) {
            fail(e);
        }
    }

    /**
     * Check there is a exception if database can not be constructed.
     */
    @Test
    void exceptionOnReadonlyDb() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
            conn.setReadOnly(true);
            new TagDbConn(new ConstSource<>(conn)).value();
            fail("Should throw a exception but not actually.");
        } catch (SQLException e) {
            assertTrue(true);
        }
    }
}
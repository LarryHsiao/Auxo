package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Decorator to generate fake data to tag db.
 */
public class FakeDataConn implements Source<Connection> {
    private final Source<Connection> dbSource;

    public FakeDataConn(Source<Connection> dbSource) {
        this.dbSource = dbSource;
    }

    @Override
    public Connection value() {
        try {
            Connection conn = dbSource.value();
            conn.createStatement().execute(
                // language=SQLite
                "insert into files(name) values ('filename');"
            );
            conn.createStatement().execute(
                // language=SQLite
                "insert into files(name) values ('filename2');");
            conn.createStatement().execute(
                // language=SQLite
                "insert into tags(name) values ('tag');");
            conn.createStatement().execute(
                // language=SQLite
                "insert into tags(name) values ('tag2');");
            conn.createStatement().execute(
                // language=SQLite
                "insert into file_tag(file_id, tag_id) values (1, 1);");
            conn.createStatement().execute(
                // language=SQLite
                "insert into file_tag(file_id, tag_id) values (1, 2);");
            conn.createStatement().execute(
                // language=SQLite
                "insert into file_tag(file_id, tag_id) values (2, 2);");
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

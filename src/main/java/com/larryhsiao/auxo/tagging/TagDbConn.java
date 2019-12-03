package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Db connection for tagging.
 */
public class TagDbConn implements Source<Connection> {
    private final Source<Connection> connSource;

    public TagDbConn(Source<Connection> connSource) {
        this.connSource = connSource;
    }

    @Override
    public Connection value() {
        Connection conn = this.connSource.value();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                // language=SQLite
                "create table tags (" +
                    "id integer primary key autoincrement, " +
                    "name text not null," +
                    "unique (name)" +
                    ");"
            );
            stmt.execute(
                // language=SQLite
                "create table files(" +
                    "id integer primary key autoincrement, " +
                    "name text not null," +
                    "unique (name)" +
                    ");"
            );
            stmt.execute(
                // language=SQLite
                "create table file_tag(" +
                    "id integer primary key autoincrement," +
                    "file_id integer not null ," +
                    "tag_id integer not null ," +
                    "unique (file_id, tag_id)" +
                    ");"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
}

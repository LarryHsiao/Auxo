package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.database.sqlite.SQLiteConn;
import com.silverhetch.clotho.source.ConstSource;

import java.nio.file.FileSystems;
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

    public TagDbConn() {
        this.connSource = new SQLiteConn(
            FileSystems.getDefault().getPath("auxo.db").toFile().getName()
        );
    }

    @Override
    public Connection value() {
        Connection conn = this.connSource.value();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                // language=SQLite
                "create table IF NOT EXISTS tags (" +
                    "id integer primary key autoincrement, " +
                    "name text not null," +
                    "unique (name)" +
                    ");"
            );
            stmt.execute(
                // language=SQLite
                "create table IF NOT EXISTS files(" +
                    "id integer primary key autoincrement, " +
                    "name text not null," +
                    "unique (name)" +
                    ");"
            );
            stmt.execute(
                // language=SQLite
                "create table IF NOT EXISTS file_tag(" +
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

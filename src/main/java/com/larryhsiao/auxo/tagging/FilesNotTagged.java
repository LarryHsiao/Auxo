package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Source to query files by command
 */
public class FilesNotTagged implements Source<ResultSet> {
    private final Source<Connection> db;

    public FilesNotTagged(Source<Connection> db) {
        this.db = db;
    }

    @Override
    public ResultSet value() {
        try {
            PreparedStatement stmt = db.value().prepareStatement(
                // language=SQLite
                "SELECT files.* FROM files " +
                    "WHERE id NOT IN (SELECT file_id from file_tag);"
            );
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

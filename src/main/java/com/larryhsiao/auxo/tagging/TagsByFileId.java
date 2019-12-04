package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Source to build tag that belongs to given file id.
 */
public class TagsByFileId implements Source<ResultSet> {
    private final Source<Connection> db;
    private final long id;

    public TagsByFileId(Source<Connection> db, long id) {
        this.db = db;
        this.id = id;
    }

    @Override
    public ResultSet value() {
        try {
            final PreparedStatement stmt = db.value().prepareStatement(
                // language=SQLite
                "SELECT t.* " +
                    "FROM file_tag " +
                    "JOIN files f on file_tag.file_id = f.id " +
                    "JOIN tags t on file_tag.tag_id= t.id " +
                    "WHERE f.id=?;"
            );
            stmt.setLong(1, id);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

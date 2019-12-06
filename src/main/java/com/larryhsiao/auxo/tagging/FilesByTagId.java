package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Source to fetch all file entry in database have tag attached.
 */
public class FilesByTagId implements Source<ResultSet> {
    private final Source<Connection> db;
    private final long tagId;

    /**
     * @param db    The connection of tagging database.
     * @param tagId The tag we wonder to search with.
     */
    public FilesByTagId(Source<Connection> db, long tagId) {
        this.db = db;
        this.tagId = tagId;
    }

    @Override
    public ResultSet value() {
        try {
            PreparedStatement stmt = db.value().prepareStatement(  // language=SQLite
                "SELECT f.* FROM file_tag " +
                    "LEFT JOIN files f on file_tag.file_id = f.id " +
                    "WHERE file_tag.tag_id=?1;");
            stmt.setLong(1, tagId);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

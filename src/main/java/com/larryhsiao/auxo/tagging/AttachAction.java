package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Action;
import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Action to attach given tag to a file.
 */
public class AttachAction implements Action {
    private final Source<Connection> dbSrc;
    private final long fileId;
    private final long tagId;

    public AttachAction(Source<Connection> dbSrc, long fileId, long tagId) {
        this.dbSrc = dbSrc;
        this.fileId = fileId;
        this.tagId = tagId;
    }

    @Override
    public void fire() {
        try (PreparedStatement stmt = dbSrc.value().prepareStatement(
            // language=SQLite
            "INSERT INTO file_tag (file_id, tag_id) " +
                "VALUES (?, ?);"
        )) {
            stmt.setLong(1, fileId);
            stmt.setLong(2, tagId);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

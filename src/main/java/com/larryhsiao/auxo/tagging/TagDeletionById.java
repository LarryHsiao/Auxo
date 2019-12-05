package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Action;
import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Action to delete a tag from database and its relations.
 */
public class TagDeletionById implements Action {
    private final Source<Connection> connSrc;
    private final long id;

    public TagDeletionById(Source<Connection> connSrc, long id) {
        this.connSrc = connSrc;
        this.id = id;
    }

    @Override
    public void fire() {
        deleteFromTags();
        // TODO: Use SQL trigger to do the relation removing
        deleteFromFileTag();
    }

    private void deleteFromTags() {
        try (PreparedStatement stmt = connSrc.value().prepareStatement(
            // language=SQLite
            "DELETE FROM tags WHERE id=?;"
        )) {
            stmt.setLong(1, id);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteFromFileTag() {
        try (PreparedStatement stmt = connSrc.value().prepareStatement(
            // language=SQLite
            "DELETE FROM file_tag WHERE tag_id=?;"
        )) {
            stmt.setLong(1, id);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

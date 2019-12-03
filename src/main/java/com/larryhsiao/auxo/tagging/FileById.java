package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.*;

/**
 * Source to fetch all file entry in database.
 */
public class FileById implements Source<ResultSet> {
    private final Source<Connection> db;
    private final long id;

    /**
     * @param db The connection of tagging database.
     * @param id Id of the file.
     */
    public FileById(Source<Connection> db, long id) {
        this.db = db;
        this.id = id;
    }

    @Override
    public ResultSet value() {
        try {
            PreparedStatement stmt = db.value().prepareStatement(
                // language=SQLite
                "SELECT * FROM files WHERE id=?;"
            );
            stmt.setLong(1, id);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

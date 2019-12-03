package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Source to fetch all file entry in database.
 */
public class AllFiles implements Source<ResultSet> {
    private final Source<Connection> db;

    /**
     * @param db The connection of tagging database.
     */
    public AllFiles(Source<Connection> db) {
        this.db = db;
    }

    @Override
    public ResultSet value() {
        try {
            Statement stmt = db.value().createStatement();
            return stmt.executeQuery(
                // language=SQLite
                "SELECT * FROM files;"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

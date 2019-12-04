package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.*;

/**
 * Source to fetch all file entry in database have given keyword.
 */
public class FilesByKeyword implements Source<ResultSet> {
    private final Source<Connection> db;
    private final String keyword;

    /**
     * @param db      The connection of tagging database.
     * @param keyword The keyword the file should related.
     */
    public FilesByKeyword(Source<Connection> db, String keyword) {
        this.db = db;
        this.keyword = keyword;
    }

    @Override
    public ResultSet value() {
        try {
            PreparedStatement stmt = db.value().prepareStatement(  // language=SQLite
                "SELECT files.* FROM files " +
                    "LEFT OUTER JOIN file_tag ft on files.id = ft.file_id " +
                    "LEFT OUTER JOIN tags t on ft.tag_id = t.id " +
                    "WHERE files.name like (?1) " +
                    "OR t.name like (?1)" +
                    "GROUP BY files.id;");
            stmt.setString(1, "%" + keyword + "%");
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

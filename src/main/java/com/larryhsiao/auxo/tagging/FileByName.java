package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.source.ConstSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Source to fetch file entry in database.
 * Create one if not exist.
 */
public class FileByName implements Source<AFile> {
    private final Source<Connection> db;
    private final String fileName;

    /**
     * @param db       The connection of tagging database.
     * @param fileName The file name
     */
    public FileByName(Source<Connection> db, String fileName) {
        this.db = db;
        this.fileName = fileName;
    }

    @Override
    public AFile value() {
        try (PreparedStatement stmt = db.value().prepareStatement(
            // language=SQLite
            "SELECT * FROM files WHERE name=?;"
        )) {
            stmt.setString(1, fileName);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return new QueriedAFile(new ConstSource<>(result)).value();
            } else {
                return new CreatedAFile(db, fileName).value();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

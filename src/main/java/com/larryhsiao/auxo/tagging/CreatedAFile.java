package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Source to build {@link AFile} by given name.
 */
public class CreatedAFile implements Source<AFile> {
    private final Source<Connection> conn;
    private final String fileName;

    public CreatedAFile(Source<Connection> conn, String fileName) {
        this.conn = conn;
        this.fileName = fileName;
    }

    @Override
    public AFile value() {
        try (PreparedStatement stmt = conn.value().prepareStatement(
            // language=SQLite
            "INSERT INTO files(name) VALUES (?);"
        )) {
            stmt.setString(1, fileName);
            stmt.execute();
            ResultSet idRes = stmt.getGeneratedKeys();
            if (idRes.next()) {
                return new ConstAFile(idRes.getLong(1), fileName);
            }
            throw new SQLException("File insert failed: " + fileName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

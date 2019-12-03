package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Source to build an {@link AFile} from db querying result.
 */
public class QueriedAFile implements Source<AFile> {
    private final Source<ResultSet> resSource;

    public QueriedAFile(Source<ResultSet> resSource) {
        this.resSource = resSource;
    }

    @Override
    public AFile value() {
        try (ResultSet res = resSource.value()){
            return new ConstAFile(
                res.getLong("id"),
                res.getString("name")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

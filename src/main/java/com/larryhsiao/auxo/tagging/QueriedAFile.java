package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import javax.xml.transform.Result;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Source to build an {@link AFile} from db querying result.
 */
public class QueriedAFile implements Source<AFile> {
    private final Source<ResultSet> resSource;
    private final boolean autoClose;

    public QueriedAFile(Source<ResultSet> resSource) {
        this(resSource, true);
    }

    public QueriedAFile(Source<ResultSet> resSource, boolean autoClose) {
        this.resSource = resSource;
        this.autoClose = autoClose;
    }

    @Override
    public AFile value() {
        try {
            ResultSet res = resSource.value();
            AFile result = new ConstAFile(
                res.getLong("id"),
                res.getString("name")
            );
            if (autoClose) {
                res.close();
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

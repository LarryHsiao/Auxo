package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Source to build {@link Tag} from querying result.
 */
public class QueriedTag implements Source<Tag> {
    private final Source<ResultSet> resSrc;
    private final boolean autoClose;

    public QueriedTag(Source<ResultSet> resSrc) {
        this(resSrc, true);
    }

    public QueriedTag(Source<ResultSet> resSrc, boolean autoClose) {
        this.resSrc = resSrc;
        this.autoClose = autoClose;
    }

    @Override
    public Tag value() {
        try {
            final ResultSet res = resSrc.value();
            Tag tag = new ConstTag(
                res.getLong("id"),
                res.getString("name")
            );
            if (autoClose) {
                res.close();
            }
            return tag;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

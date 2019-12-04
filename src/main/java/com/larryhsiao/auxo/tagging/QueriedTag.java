package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Source to build {@link Tag} from querying result.
 */
public class QueriedTag implements Source<Tag> {
    private final Source<ResultSet> resSrc;

    public QueriedTag(Source<ResultSet> resSrc) {
        this.resSrc = resSrc;
    }

    @Override
    public Tag value() {
        try (ResultSet res = resSrc.value()) {
            return new ConstTag(
                res.getLong("id"),
                res.getString("name")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

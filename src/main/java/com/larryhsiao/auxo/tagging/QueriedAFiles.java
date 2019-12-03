package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Source to build map of {@link AFile} from db querying {@link ResultSet}.
 */
public class QueriedAFiles implements Source<Map<String, AFile>> {
    private final Source<ResultSet> resSource;

    public QueriedAFiles(Source<ResultSet> resSource) {
        this.resSource = resSource;
    }

    @Override
    public Map<String, AFile> value() {
        try {
            ResultSet res = resSource.value();
            Map<String, AFile> result = new HashMap<>();
            while (res.next()) {
                final String name = res.getString("name");
                result.put(
                    name,
                    new ConstAFile(name)
                );
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

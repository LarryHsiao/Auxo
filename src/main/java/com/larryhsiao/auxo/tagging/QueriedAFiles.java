package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.source.ConstSource;

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
        try (final ResultSet res = resSource.value()){
            final Map<String, AFile> result = new HashMap<>();
            while (res.next()) {
                final AFile file = new QueriedAFile(new ConstSource<>(res), false).value();
                result.put(file.name(), file);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Source to build {@link AFile} map that file system also exist.
 */
public class FsAFile implements Source<Map<String, AFile>> {
    private final Source<Map<String, AFile>> origin;
    private final Source<Set<String>> intersectionSource;

    public FsAFile(
        Source<Map<String, AFile>> originSource,
        Source<Set<String>> intersectionSource
    ) {
        this.origin = originSource;
        this.intersectionSource = intersectionSource;
    }

    @Override
    public Map<String, AFile> value() {
        final Set<String> intersection = intersectionSource.value();
        final HashMap<String, AFile> result = new HashMap<>();
        for (AFile file : origin.value().values()) {
            if (intersection.contains(file.name())) {
                result.put(file.name(), file);
            }
        }
        return result;
    }
}

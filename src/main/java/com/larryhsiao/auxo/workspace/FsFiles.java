package com.larryhsiao.auxo.workspace;

import com.larryhsiao.auxo.utils.FileComparator;
import com.larryhsiao.clotho.Source;
import com.larryhsiao.clotho.utility.comparator.StringComparator;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Return all files in workspace.
 */
public class FsFiles implements Source<Map<String, File>> {
    private final File workspace;

    public FsFiles(File workspace) {
        this.workspace = workspace;
    }

    @Override
    public Map<String, File> value() {
        if (!workspace.isDirectory()) {
            throw new RuntimeException("The given workspace is not a directory: " + workspace.getAbsolutePath());
        }
        final LinkedHashMap<String, File> result = new LinkedHashMap<>();
        final File[] files = workspace.listFiles();
        if (files != null) {
            List<File> fileList = Arrays.asList(files);
            final StringComparator comparator = new StringComparator();
            fileList.sort(new FileComparator((o1, o2) ->
                comparator.compare(o2.getName(), o1.getName())));
            for (File file : fileList) {
                if (file.getName().startsWith(".auxo.h2")) {
                    continue;
                }
                result.put(file.getName(), file);
            }
        }
        return result;
    }
}

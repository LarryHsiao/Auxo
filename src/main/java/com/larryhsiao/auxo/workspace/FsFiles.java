package com.larryhsiao.auxo.workspace;

import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.utility.comparator.StringComparator;

import java.io.File;
import java.nio.file.FileSystems;
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

    public FsFiles() {
        this.workspace = FileSystems.getDefault().getPath(".").toFile();
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
            fileList.sort((o1, o2) -> comparator.compare(o2.getName(), o1.getName()));
            for (File file : fileList) {
                result.put(file.getName(), file);
            }
        }
        return result;
    }
}

package com.larryhsiao.auxo.workspace;

import com.silverhetch.clotho.Source;

import java.io.File;
import java.util.HashMap;
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
            throw new RuntimeException("The given workspace is not a directory.");
        }
        HashMap<String, File> result = new HashMap<>();
        File[] files = workspace.listFiles();
        if (files != null) {
            for (File file : files) {
                result.put(file.getName().replace(
                    workspace.getAbsolutePath(), ""),
                    file
                );
            }
        }
        return result;
    }
}

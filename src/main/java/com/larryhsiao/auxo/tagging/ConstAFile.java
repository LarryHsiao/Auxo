package com.larryhsiao.auxo.tagging;

/**
 * Const of {@link AFile}
 */
public class ConstAFile implements AFile {
    private final String name;

    public ConstAFile(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }
}

package com.larryhsiao.auxo.tagging;

/**
 * Const of {@link AFile}
 */
public class ConstAFile implements AFile {
    private final long id;
    private final String name;

    public ConstAFile(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }
}

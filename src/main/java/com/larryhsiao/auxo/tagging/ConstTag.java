package com.larryhsiao.auxo.tagging;

/**
 * Const of {@link Tag}
 */
public class ConstTag implements Tag {
    private final long id;
    private final String name;

    public ConstTag(long id, String name) {
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

package com.larryhsiao.auxo.tagging;

/**
 * Represent a tag.
 */
public interface Tag {
    /**
     * @return Id of this instance.
     */
    long id();

    /**
     * @return Name of this tag
     */
    String name();
}

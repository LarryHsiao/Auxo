package com.larryhsiao.auxo.tagging;

/**
 * Represent a file that managed by Auxo.
 */
public interface AFile {
    /**
     * @return Id of this {@link AFile}
     */
    long id();
    /**
     * The name of this file in workspace.
     */
    String name();
}

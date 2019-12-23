package com.larryhsiao.auxo.views;

import com.larryhsiao.juno.Tag;
import com.larryhsiao.juno.TagByName;
import com.silverhetch.clotho.Source;
import javafx.util.StringConverter;

import java.sql.Connection;

/**
 * Adapter for Tag to string.
 */
public class TagStringConverter extends StringConverter<Tag> {
    private final Source<Connection> db;

    public TagStringConverter(Source<Connection> db) {
        this.db = db;
    }

    @Override
    public String toString(Tag object) {
        return object.name();
    }

    @Override
    public Tag fromString(String string) {
        return new TagByName(
            db,
            string
        ).value();
    }
}

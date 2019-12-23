package com.larryhsiao.auxo.tagging;

import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * Source to fetch all file entry in database have given input.
 *
 * There are two type of input keyword, command and keyword.
 * Command is a string that meets specific format like '#!tags' which means querying files that no tags attached.
 * Keyword is a string that related resource have.
 */
public class FilesByInput implements Source<ResultSet> {
    private final Source<Connection> db;
    private final String input;

    /**
     * @param db      The connection of tagging database.
     * @param input The command or keyword of file.
     */
    public FilesByInput(Source<Connection> db, String input) {
        this.db = db;
        this.input = input;
    }

    @Override
    public ResultSet value() {
        if ("#!tag".equals(input)) {
            return new FilesNotTagged(db).value();
        }
        return new FilesByKeyword(db, input).value();
    }
}

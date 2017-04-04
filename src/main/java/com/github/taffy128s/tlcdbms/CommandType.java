package com.github.taffy128s.tlcdbms;

/**
 * Command Type, such as
 * CREATE, INSERT, SELECT, SHOW, DESC, EXIT, QUIT.
 */
public enum CommandType {
    NONE,
    CREATE,
    INSERT,
    SELECT,
    DROP,
    SHOW_TABLE_LIST,
    SHOW_TABLE_CONTENT,
    DESC,
    LOAD,
    EXIT,
    QUIT
}

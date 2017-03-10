package com.github.taffy128s.tlcdbms.sqlparsers;

/**
 * SQL Keywords in this project.
 */
public class SQLKeyWords {
    private static final String[] KEYWORD_STRINGS = {
            "CREATE",
            "INSERT",
            "SELECT",
            "TABLE",
            "TABLES",
            "INTO",
            "VALUES",
            "FROM",
            "WHERE",
            "AS",
            "INT",
            "VARCHAR"
    };

    /**
     * Check whether <code>string</code> is a keyword.
     *
     * @param string string to check.
     * @return true if it's keyword, false otherwise.
     */
    public static boolean isSQLKeyword(String string) {
        for (String str : KEYWORD_STRINGS) {
            if (str.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }
}

package com.github.taffy128s.tlcdbms.sqlparsers;

public class SQLKeyWords {
    private static final String[] KEYWORD_STRINGS = {
            "CREATE",
            "INSERT",
            "SELECT",
            "TABLE",
            "INTO",
            "VALUES",
            "WHERE",
            "INT",
            "VARCHAR"
    };

    public static boolean isSQLKeyword(String string) {
        for (String str : KEYWORD_STRINGS) {
            if (str.equals(string)) {
                return true;
            }
        }
        return false;
    }
}

package com.github.taffy128s.tlcdbms;

public class State {
    public static int START = 0;
    public static int ERROR = 1;
    public static int CREATE_PARSED = 101;
    public static int CREATE_TABLE_PARSED = 102;
    public static int CREATE_TABLE_NAME_PARSED = 103;
    public static int CREATE_TABLE_LEFT_PARSED = 104;
    public static int CREATE_TABLE_CONTENT_NAME_PARSED = 105;
    public static int CREATE_TABLE_CONTENT_INT_PARSED = 106;
    public static int CREATE_TABLE_CONTENT_CHAR_PARSED = 107;
    public static int CREATE_TABLE_CONTENT_CHAR_LEFT_PARSED = 108;
    public static int CREATE_TABLE_CONTENT_CHAR_NUM_PARSED = 109;
    public static int CREATE_TABLE_CONTENT_CHAR_RIGHT_PARSED = 110;
    public static int CREATE_TABLE_CONTENT_PRIMARY_PARSED = 111;
    public static int CREATE_TABLE_CONTENT_PRIMARY_KEY_PARSED = 112;
    public static int CREATE_TABLE_RIGHT_PARSED = 113;
}

package com.github.taffy128s.tlcdbms;

/**
 * Select command query type.
 * NORMAL: normal cases.
 * SUM: sum() function.
 * COUNT: count() function.
 */
public enum QueryType {
    NORMAL,
    SUM,
    COUNT
}

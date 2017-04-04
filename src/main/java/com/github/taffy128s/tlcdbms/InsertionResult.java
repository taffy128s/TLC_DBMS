package com.github.taffy128s.tlcdbms;

/**
 * Insertion Result.
 * Returned by Table.checkInputData().
 */
public enum InsertionResult {
    SUCCESS,
    DUPLICATED_DATA_TUPLE,
    DUPLICATED_PRIMARY_KEY,
    NULL_PRIMARY_KEY
}

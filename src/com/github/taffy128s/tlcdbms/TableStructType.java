package com.github.taffy128s.tlcdbms;

/**
 * Table type, such as b plus tree table, hash table.
 */
public enum TableStructType {
    SETTABLE,
    BPLUSTREE,
    BTREE,
    RBTREE,
    HASH
}

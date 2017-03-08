package com.github.taffy128s.tlcdbms;

/**
 * DataType
 * INT or VARCHAR with mLimit length
 */
public class DataType {
    private DataTypeIdentifier mType;
    private int mLimit;

    public DataType(DataTypeIdentifier typeIdentifier, int limit) {
        mType = typeIdentifier;
        mLimit = limit;
    }

    public DataTypeIdentifier getType() {
        return mType;
    }

    public int getLimit() {
        return mLimit;
    }
}

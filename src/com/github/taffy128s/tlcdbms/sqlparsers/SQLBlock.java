package com.github.taffy128s.tlcdbms.sqlparsers;

public class SQLBlock {
    private boolean mValid;
    private String mValue;

    public SQLBlock(String value, boolean valid) {
        mValue = value;
        mValid = valid;
    }

    public String getValue() {
        return mValue;
    }

    public boolean isValid() {
        return mValid;
    }
}

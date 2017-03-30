package com.github.taffy128s.tlcdbms;

public class Target {
    
    private String mTableName;
    private String mAttribute;
    private int mAttributeIndex;
    
    public Target(String tableName, String attr) {
        mTableName = tableName;
        mAttribute = attr;
        mAttributeIndex = -1;
    }
    
    public String getTableName() {
        return mTableName;
    }

    public void setTableName(String tableName) {
        mTableName = tableName;
    }

    public String getAttribute() {
        return mAttribute;
    }

    public void setAttribute(String leftAttribute) {
        mAttribute = leftAttribute;
    }
    
    public int getAttributeIndex() {
        return mAttributeIndex;
    }

    public void setAttributeIndex(int index) {
        mAttributeIndex = index;
    }
    
    public String toString() {
        if (mTableName == null) {
            return mAttribute;
        } else {
            return mTableName + "." + mAttribute;
        }
    }
    
}

package com.github.taffy128s.tlcdbms;

public class Target {
    
    private String mTableName;
    private String mAttribute;
    private int mAttributeIndex;
    
    /**
     * Constructor.
     * 
     * @param tableName table name to use.
     * @param attr attribute to use.
     */
    public Target(String tableName, String attr) {
        mTableName = tableName;
        mAttribute = attr;
        mAttributeIndex = -1;
    }
    
    /**
     * Table name getter.
     * 
     * @return table name.
     */
    public String getTableName() {
        return mTableName;
    }

    /**
     * Table name setter.
     * 
     * @param tableName table name to set.
     */
    public void setTableName(String tableName) {
        mTableName = tableName;
    }

    /**
     * Attribute getter.
     * 
     * @return attribute.
     */
    public String getAttribute() {
        return mAttribute;
    }

    /**
     * Attribute setter.
     * 
     * @param attribute attribute to set.
     */
    public void setAttribute(String attribute) {
        mAttribute = attribute;
    }
    
    /**
     * Attribute index getter.
     * 
     * @return attribute index.
     */
    public int getAttributeIndex() {
        return mAttributeIndex;
    }

    /**
     * Attribute index setter.
     * 
     * @param index index to set.
     */
    public void setAttributeIndex(int index) {
        mAttributeIndex = index;
    }
    
    /**
     * transform this class to a string.
     */
    public String toString() {
        if (mTableName == null) {
            return mAttribute;
        } else {
            return mTableName + "." + mAttribute;
        }
    }
    
}

package com.github.taffy128s.tlcdbms;

/**
 * Query target used in select command.
 */
public class Target {
    private String mTableName;
    private String mAttribute;

    /**
     * Constructor.
     *
     * @param tableName table name to use.
     * @param attr attribute to use.
     */
    public Target(String tableName, String attr) {
        mTableName = tableName;
        mAttribute = attr;
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

    @Override
    public String toString() {
        if (mTableName == null) {
            return mAttribute;
        } else {
            return mTableName + "." + mAttribute;
        }
    }

}

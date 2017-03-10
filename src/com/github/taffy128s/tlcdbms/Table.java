package com.github.taffy128s.tlcdbms;

import java.util.ArrayList;

/**
 * Database Table.
 *
 * It contains all operations related to the table in DB,
 * including CREATE, INSERT, SELECT...
 */
public abstract class Table {
    public static final String AUTO_PRIMARY_KEY_NAME = "$__AUTO_PRIMARY_KEY__";

    protected String mTablename;
    protected ArrayList<String> mAttributeNames;
    protected ArrayList<DataType> mAttributeTypes;
    protected int mPrimaryKey;
    protected int mPrimaryKeyCounter;

    /**
     * Initialize a Table.
     */
    public Table() {
        mTablename = "";
        mAttributeNames = new ArrayList<>();
        mAttributeTypes = new ArrayList<>();
        mPrimaryKey = -1;
        mPrimaryKeyCounter = 0;
    }

    /**
     * Initialize a table with attribute names, types and
     * primary key (-1 if no primary key).
     *
     * @param attributeNames an array list of names.
     * @param attributeTypes an array list of types.
     * @param primaryKey primary key index, -1 if none.
     */
    public Table(String tablename, ArrayList<String> attributeNames, ArrayList<DataType> attributeTypes, int primaryKey) {
        this();
        mTablename = tablename;
        mAttributeNames = attributeNames;
        mAttributeTypes = attributeTypes;
        mPrimaryKey = primaryKey;
        if (mPrimaryKey == -1) {
            attributeNames.add(Table.AUTO_PRIMARY_KEY_NAME);
            attributeTypes.add(new DataType(DataTypeIdentifier.INT, -1));
        }
    }

    /**
     * Set the index of the primary key.
     *
     * @param primaryKey primary key index.
     */
    public void setPrimaryKey(int primaryKey) {
        mPrimaryKey = primaryKey;
    }

    /**
     * Get the index of the primary key.
     *
     * @return primary key index.
     */
    public int getPrimaryKey() {
        return mPrimaryKey;
    }

    /**
     * Set all attribute names of this table.
     *
     * @param attributeNames an list of attribute names.
     */
    public void setAttributeNames(ArrayList<String> attributeNames) {
        mAttributeNames = attributeNames;
    }

    /**
     * Get all attribute names.
     *
     * @return a list of all attribute names.
     */
    public ArrayList<String> getAttributeNames() {
        return mAttributeNames;
    }

    /**
     * Set all attribute types of this table.
     *
     * @param attributeTypes an list of attribute types.
     */
    public void setAttributeTypes(ArrayList<DataType> attributeTypes) {
        mAttributeTypes = attributeTypes;
    }

    /**
     * Get all attribute types.
     *
     * @return a list of all attribute types.
     */
    public ArrayList<DataType> getAttributeTypes() {
        return mAttributeTypes;
    }

    /**
     * Override Object.toString().
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return ("Table " + mTablename) + "\n";
    }

    /**
     * Insert a data record into table.
     * Assume that data is with valid type.
     *
     * @param data data record to be inserted.
     * @return true if succeed, false if failed.
     */
    public abstract boolean insert(DataRecord data);

    /**
     * Get all records in the table.
     *
     * @return an array list of all records.
     */
    public abstract ArrayList<DataRecord> getAllRecords();
}

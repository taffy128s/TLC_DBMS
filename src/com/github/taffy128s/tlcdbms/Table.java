package com.github.taffy128s.tlcdbms;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Database Table.
 *
 * It contains all operations related to the table in DB,
 * including CREATE, INSERT, SELECT...
 */
public class Table {
    public static final String AUTO_PRIMARY_KEY_NAME = "$__AUTO_PRIMARY_KEY__";

    private String mTablename;
    private ArrayList<String> mAttributeNames;
    private ArrayList<DataType> mAttributeTypes;
    private HashSet<DataRecord> mTable;
    private HashSet<Object> mPrimaryTable;
    private int mPrimaryKey;
    private int mPrimaryKeyCounter;

    /**
     * Initialize a Table.
     */
    public Table() {
        mTablename = "";
        mAttributeNames = new ArrayList<>();
        mAttributeTypes = new ArrayList<>();
        mTable = new HashSet<>();
        mPrimaryTable = new HashSet<>();
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
     * Insert a data record into table.
     * Assume that data is with valid type.
     *
     * @param data data record to be inserted.
     * @return true if succeed, false if failed.
     */
    public boolean insert(DataRecord data) {
        if (mTable.contains(data)) {
            System.out.println("Data tuple already exists in table.");
            return false;
        } else if (mPrimaryKey != -1 && data.get(mPrimaryKey) == null) {
            System.out.println("Primary key cannot be null.");
            return false;
        } else if (mPrimaryKey != -1 && checkPrimaryKey(data)) {
            System.out.println("Primary key " + data.get(mPrimaryKey) + " already exists in table.");
            return false;
        } else {
            if (mPrimaryKey != -1) {
                mTable.add(data);
                mPrimaryTable.add(data.get(mPrimaryKey));
            } else {
                while (mPrimaryTable.contains(mPrimaryKeyCounter)) {
                    ++mPrimaryKeyCounter;
                }
                data.set(data.length() - 1, mPrimaryKeyCounter);
                mTable.add(data);
                mPrimaryTable.add(mPrimaryKeyCounter);
            }
            return true;
        }
    }

    /**
     * Check whether the primary key is already in table.
     *
     * @param data data record to be inserted.
     * @return true if the primary key is valid, false if invalid.
     */
    private boolean checkPrimaryKey(DataRecord data) {
        return mPrimaryKey == -1 || mPrimaryTable.contains(data.get(mPrimaryKey));
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
     * Get all records in the table.
     *
     * @return an array list of all records.
     */
    public ArrayList<DataRecord> getAllRecords() {
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord records : mTable) {
            result.add(records);
        }
        return result;
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
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        Object[] records = mTable.toArray();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mTablename).append("\n");
        for (Object record : records) {
            stringBuilder.append(record.toString());
        }
        return stringBuilder.toString();
    }
}

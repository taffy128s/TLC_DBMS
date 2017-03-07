package com.github.taffy128s.tlcdbms;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Database Table
 *
 * It contains all operations related to the table in DB,
 * including CREATE, INSERT, DELETE...
 */
public class Table {
    private ArrayList<String> mAttributeNames;
    private ArrayList<DataType> mAttributeTypes;
    private HashSet<DataRecord> mTable;
    private HashSet<Object> mPrimaryTable;
    private int mPrimaryKey;

    /**
     * Initialize a Table
     */
    public Table() {
        mAttributeNames = new ArrayList<>();
        mAttributeTypes = new ArrayList<>();
        mTable = new HashSet<>();
        mPrimaryTable = new HashSet<>();
        mPrimaryKey = -1;
    }

    /**
     * Insert a data record into table.
     *
     * @param data data record to be inserted
     * @return true if succeed, false if failed
     */
    public boolean insert(DataRecord data) {
        if (mTable.contains(data)) {
            return false;
        } else if (mPrimaryKey != -1 && checkPrimaryKey(data)) {
            return false;
        } else {
            mTable.add(data);
            return true;
        }
    }

    private boolean checkPrimaryKey(DataRecord data) {
        return mPrimaryTable.contains(data.get(mPrimaryKey));
    }

    public boolean setPrimaryKey(int primaryKey) {
        mPrimaryKey = primaryKey;
        return true;
    }

    public int getPrimaryKey() {
        return mPrimaryKey;
    }

    public DataRecord[] getAllRecords() {
        return (DataRecord[]) mTable.toArray();
    }

    public boolean setAttributeNames(ArrayList<String> attributeNames) {
        mAttributeNames = attributeNames;
        return true;
    }

    public ArrayList<String> getAttributeNames() {
        return mAttributeNames;
    }

    public boolean setAttributeTypes(ArrayList<DataType> attributeTypes) {
        mAttributeTypes = attributeTypes;
        return true;
    }

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
        DataRecord[] records = (DataRecord[]) mTable.toArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (DataRecord record : records) {
            stringBuilder.append(record.toString());
        }
        return stringBuilder.toString();
    }
}

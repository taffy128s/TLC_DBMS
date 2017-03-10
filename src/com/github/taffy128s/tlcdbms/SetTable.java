package com.github.taffy128s.tlcdbms;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Database Table.
 * Structure: HashSet (java built-in).
 *
 * It contains all operations related to the table in DB,
 * including CREATE, INSERT, SELECT...
 */
public class SetTable extends Table {
    private HashSet<DataRecord> mTable;
    private HashSet<Object> mPrimaryTable;

    /**
     * Initialize a SetTable.
     */
    public SetTable() {
        super();
        mTable = new HashSet<>();
        mPrimaryTable = new HashSet<>();
    }

    /**
     * Initialize a set table with attribute names, types and
     * primary key (-1 if no primary key).
     *
     * @param attributeNames an array list of names.
     * @param attributeTypes an array list of types.
     * @param primaryKey primary key index, -1 if none.
     */
    public SetTable(String tablename, ArrayList<String> attributeNames, ArrayList<DataType> attributeTypes, int primaryKey) {
        super(tablename, attributeNames, attributeTypes, primaryKey);
        mTable = new HashSet<>();
        mPrimaryTable = new HashSet<>();
    }

    /**
     * Insert a data record into table.
     * Assume that data is with valid type.
     *
     * @param data data record to be inserted.
     * @return true if succeed, false if failed.
     */
    @Override
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
     * Get all records in the table.
     *
     * @return an array list of all records.
     */
    @Override
    public ArrayList<DataRecord> getAllRecords() {
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord records : mTable) {
            result.add(records);
        }
        return result;
    }

    /**
     * Override Object.toString().
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

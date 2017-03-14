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

    @Override
    public boolean insert(DataRecord dataRecord) {
        if (mTable.contains(dataRecord)) {
            System.out.println("Data tuple already exists in table.");
            return false;
        } else if (mPrimaryKey != -1 && dataRecord.get(mPrimaryKey) == null) {
            System.out.println("Primary key cannot be null.");
            return false;
        } else if (mPrimaryKey != -1 && checkPrimaryKey(dataRecord)) {
            System.out.println("Primary key " + dataRecord.get(mPrimaryKey) + " already exists in table.");
            return false;
        } else {
            // WHAT THE SPEC.
            // setAutoPrimaryKey(data);
            mTable.add(dataRecord);
            if (mPrimaryKey != -1) {
                mPrimaryTable.add(dataRecord.get(mPrimaryKey));
            }
            return true;
        }
    }

    @Override
    public boolean insertAll(ArrayList<DataRecord> dataRecords) {
        boolean result = true;
        for (DataRecord dataRecord : dataRecords) {
            boolean status = insert(dataRecord);
            result = result && status;
        }
        return result;
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

    @Override
    public ArrayList<DataRecord> getAllRecords() {
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord records : mTable) {
            result.add(records);
        }
        return result;
    }

    @Override
    public String getTableType() {
        return "SETTABLE";
    }

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

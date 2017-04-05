package com.github.taffy128s.tlcdbms;

import java.io.*;
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
    private int mKeyIndex;

    /**
     * Initialize a SetTable.
     * Note that this constructor should only be called when restoring from disk.
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
     * @param keyIndex column index of this table, -1 if none.
     */
    public SetTable(String tablename, ArrayList<String> attributeNames, ArrayList<DataType> attributeTypes, int primaryKey, int keyIndex) {
        super(tablename, attributeNames, attributeTypes, primaryKey);
        mTable = new HashSet<>();
        mPrimaryTable = new HashSet<>();
        if (keyIndex == -1) {
            keyIndex = (primaryKey == -1) ? 0 : primaryKey;
        }
        mKeyIndex = keyIndex;
    }

    @Override
    public InsertionResult checkInputData(DataRecord dataRecord) {
        if (mTable.contains(dataRecord)) {
            return InsertionResult.DUPLICATED_DATA_TUPLE;
        } else if (mPrimaryKey != -1 && dataRecord.get(mPrimaryKey) == null) {
            return InsertionResult.NULL_PRIMARY_KEY;
        } else if (!checkPrimaryKey(dataRecord)) {
            return InsertionResult.DUPLICATED_PRIMARY_KEY;
        } else {
            return InsertionResult.SUCCESS;
        }
    }

    @Override
    public boolean insert(DataRecord dataRecord) {
        mTable.add(dataRecord);
        if (mPrimaryKey != -1) {
            mPrimaryTable.add(dataRecord.get(mPrimaryKey));
        }
        return true;
    }

    @Override
    protected boolean insertAll(ArrayList<DataRecord> dataRecords) {
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
        return mPrimaryKey == -1 || !mPrimaryTable.contains(data.get(mPrimaryKey));
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
    public Table generateAliasTable(String aliasName) {
        SetTable result = new SetTable(aliasName, mAttributeNames, mAttributeTypes, mPrimaryKey, mKeyIndex);
        result.mTable = this.mTable;
        result.mPrimaryTable = this.mPrimaryTable;
        return result;
    }

    @Override
    public String getTableType() {
        return "SETTABLE";
    }

    @Override
    public TableFieldType getFieldType(int index) {
        if (index == mPrimaryKey) {
            return TableFieldType.PRIMARY_KEY;
        } else {
            return TableFieldType.NORMAL;
        }
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

    @Override
    public boolean writeToDisk(String filename) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(mTablename + "\n");
            writer.write(mAttributeNames.size() + "\n");
            for (int i = 0; i < mAttributeNames.size(); ++i) {
                writer.write(mAttributeNames.get(i) + "\0");
                writer.write(mAttributeTypes.get(i).getLimit() + "\n");
            }
            writer.write(mPrimaryKey + "\n");
            ArrayList<DataRecord> dataRecords = getAllRecords();
            writer.write(dataRecords.size() + "\n");
            for (DataRecord record : dataRecords) {
                writer.write(record.writeToString() + "\n");
            }
            writer.close();
            return true;
        } catch (IOException e) {
            System.err.println(filename + ": file I/O error.");
        }
        return false;
    }

    @Override
    public boolean restoreFromDisk(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String input;
            mTablename = reader.readLine();
            int attrSize = Integer.parseInt(reader.readLine());
            for (int i = 0; i < attrSize; ++i) {
                input = reader.readLine();
                String[] attrProperties = input.split("\0");
                mAttributeNames.add(attrProperties[0]);
                int limit = Integer.parseInt(attrProperties[1]);
                if (limit < 0) {
                    mAttributeTypes.add(new DataType(DataTypeIdentifier.INT, -1));
                } else {
                    mAttributeTypes.add(new DataType(DataTypeIdentifier.VARCHAR, limit));
                }
            }
            mSourceTables.add(mTablename);
            mPrimaryKey = Integer.parseInt(reader.readLine());
            int recordSize = Integer.parseInt(reader.readLine());
            ArrayList<DataRecord> records = new ArrayList<>();
            while ((input = reader.readLine()) != null) {
                DataRecord record = new DataRecord();
                record.restoreFromString(input);
                records.add(record);
            }
            if (records.size() != recordSize) {
                reader.close();
                return false;
            }
            insertAll(records);
            reader.close();
            return true;
        } catch (FileNotFoundException e) {
            System.err.println(filename + ": no such file or directory.");
        } catch (IOException e) {
            System.err.println(filename + ": file I/O error.");
        }
        return false;
    }
}

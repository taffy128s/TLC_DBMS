package com.github.taffy128s.tlcdbms;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Hash Table.
 */
public class HashTable extends Table {
    private HashMap<Object, ArrayList<DataRecord>> mTable;
    private ArrayList<DataRecord> mNullTable;
    private ArrayList<DataRecord> mAllRecords;
    private DataTypeIdentifier mIndexDataType;
    private int mKeyIndex;

    /**
     * Initialize a HashTable.
     * Note that this constructor should only be called when restoring from disk.
     */
    public HashTable() {
        super();
        mTable = new HashMap<>();
        mNullTable = new ArrayList<>();
        mAllRecords = new ArrayList<>();
        mIndexDataType = DataTypeIdentifier.INT;
        mKeyIndex = 0;
    }

    /**
     * Initialize a hash table with attribute names, types and
     * primary key (-1 if no primary key).
     *
     * @param attributeNames an array list of names.
     * @param attributeTypes an array list of types.
     * @param primaryKey primary key index, -1 if none.
     * @param keyIndex column index of this table, -1 if none.
     */
    public HashTable(String tablename, ArrayList<String> attributeNames, ArrayList<DataType> attributeTypes, int primaryKey, int keyIndex) {
        super(tablename, attributeNames, attributeTypes, primaryKey);
        mTable = new HashMap<>();
        mNullTable = new ArrayList<>();
        mAllRecords = new ArrayList<>();
        if (keyIndex == -1) {
            keyIndex = (primaryKey == -1) ? 0 : primaryKey;
        }
        mKeyIndex = keyIndex;
        mIndexDataType = attributeTypes.get(mKeyIndex).getType();
    }

    /**
     * Check whether data record given already exists or not.
     *
     * @param dataRecord data to check.
     * @return true if exists, false otherwise.
     */
    private boolean isDuplicatedData(DataRecord dataRecord) {
        ArrayList<DataRecord> results;
        results = mTable.get(dataRecord.get(mKeyIndex));
        if (results == null) {
            return false;
        }
        for (DataRecord record : results) {
            if (dataRecord.equals(record)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether data record given has a valid primary key.
     *
     * @param dataRecord data to check.
     * @return true if valid, false otherwise.
     */
    private boolean checkPrimaryKey(DataRecord dataRecord) {
        return mKeyIndex != mPrimaryKey || mPrimaryKey == -1 || !mTable.containsKey(dataRecord.get(mPrimaryKey));
    }

    /**
     * Insert data with key field value null.
     * Special case because null is not comparable to other key values.
     *
     * @param dataRecord data to insert.
     * @return true if succeed, false otherwise.
     */
    private boolean insertNull(DataRecord dataRecord) {
        mNullTable.add(dataRecord);
        return true;
    }

    /**
     * Get all values with key null.
     *
     * @return a list of data records.
     */
    private ArrayList<DataRecord> getNullTableRecords() {
        return mNullTable;
    }

    @Override
    public InsertionResult checkInputData(DataRecord dataRecord) {
        if (dataRecord.get(mKeyIndex) == null) {
            for (DataRecord record : mNullTable) {
                if (dataRecord.equals(record)) {
                    return InsertionResult.DUPLICATED_DATA_TUPLE;
                }
            }
            if (mPrimaryKey != -1 && dataRecord.get(mPrimaryKey) == null) {
                return InsertionResult.NULL_PRIMARY_KEY;
            }
            return InsertionResult.SUCCESS;
        }
        if (isDuplicatedData(dataRecord)) {
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
        if (dataRecord.get(mKeyIndex) == null) {
            mAllRecords.add(dataRecord);
            return insertNull(dataRecord);
        }
        if (!mTable.containsKey(dataRecord.get(mKeyIndex))) {
            ArrayList<DataRecord> newData = new ArrayList<>();
            newData.add(dataRecord);
            mTable.put(dataRecord.get(mKeyIndex), newData);
        } else {
            mTable.get(dataRecord.get(mKeyIndex)).add(dataRecord);
        }
        mAllRecords.add(dataRecord);
        appendToDisk(mFilename, dataRecord);
        return true;
    }

    @Override
    protected boolean insertAll(ArrayList<DataRecord> dataRecords) {
        if (dataRecords.isEmpty()) {
            return true;
        }
        mAllRecords.addAll(dataRecords);
        ArrayList<DataRecord> nullDataRecords = new ArrayList<>();
        ArrayList<DataRecord> notNullDataRecords = new ArrayList<>();
        for (DataRecord record : dataRecords) {
            if (record.get(mKeyIndex) == null) {
                nullDataRecords.add(record);
            } else {
                notNullDataRecords.add(record);
            }
        }
        mNullTable.addAll(nullDataRecords);
        for (DataRecord record : notNullDataRecords) {
            if (!mTable.containsKey(record.get(mKeyIndex))) {
                ArrayList<DataRecord> newData = new ArrayList<>();
                newData.add(record);
                mTable.put(record.get(mKeyIndex), newData);
            } else {
                mTable.get(record.get(mKeyIndex)).add(record);
            }
        }
        return true;
    }

    @Override
    public Table queryEqual(int columnIndex, Object key) {
        Table table = generateEmptyResultTable();
        if (mKeyIndex == columnIndex) {
            if (key == null) {
                table.insertAll(mNullTable);
            } else {
                ArrayList<DataRecord> result = mTable.get(key);
                if (result != null) {
                    table.insertAll(mTable.get(key));
                }
            }
            return table;
        } else {
            return super.queryEqual(columnIndex, key);
        }
    }

    @Override
    public Table queryNotEqual(int columnIndex, Object key) {
        Table table = generateEmptyResultTable();
        if (mKeyIndex == columnIndex) {
            if (key == null) {
                Collection<ArrayList<DataRecord>> values = mTable.values();
                for (ArrayList<DataRecord> records : values) {
                    table.insertAll(records);
                }
            } else {
                table.insertAll(mNullTable);
                for (Object keyRecord : mTable.keySet()) {
                    if (!keyRecord.equals(key)) {
                        table.insertAll(mTable.get(keyRecord));
                    }
                }
            }
            return table;
        } else {
            return super.queryEqual(columnIndex, key);
        }
    }

    @Override
    public ArrayList<DataRecord> getAllRecords() {
        return mAllRecords;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<DataRecord> getAllRecords(int sortIndex, SortingType sortingType) {
        if (sortIndex == mKeyIndex) {
            ArrayList<DataRecord> allRecords = getAllRecords();
            if (sortingType == SortingType.DESCENDING) {
                Collections.reverse(allRecords);
            }
            return allRecords;
        } else {
            return super.getAllRecords(sortIndex, sortingType);
        }
    }

    @Override
    public Table generateAliasTable(String aliasName) {
        HashTable table = new HashTable(aliasName, mAttributeNames, mAttributeTypes, mPrimaryKey, mKeyIndex);
        table.mTable = this.mTable;
        table.mNullTable = this.mNullTable;
        table.mAllRecords = this.mAllRecords;
        return table;
    }

    @Override
    public String getTableType() {
        return "HASH";
    }

    @Override
    public TableFieldType getFieldType(int index) {
        if (index == mPrimaryKey) {
            return TableFieldType.PRIMARY_KEY;
        } else if (index == mKeyIndex) {
            return TableFieldType.KEY;
        } else {
            return TableFieldType.NORMAL;
        }
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
            writer.write(mKeyIndex + "\n");
            ArrayList<DataRecord> dataRecords = getAllRecords();
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
            mFilename = "./" + DBManager.DIRNAME + "/" + mTablename + ".tlctable";
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
            mKeyIndex = Integer.parseInt(reader.readLine());
            mIndexDataType = mAttributeTypes.get(mKeyIndex).getType();
            ArrayList<DataRecord> records = new ArrayList<>();
            while ((input = reader.readLine()) != null) {
                DataRecord record = new DataRecord();
                record.restoreFromString(input);
                records.add(record);
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

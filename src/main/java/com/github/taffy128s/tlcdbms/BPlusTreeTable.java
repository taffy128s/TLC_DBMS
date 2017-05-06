package com.github.taffy128s.tlcdbms;

import com.github.taffy128s.btrees.BPlusTree;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * B Plus Tree Table.
 */
public class BPlusTreeTable extends Table {
    private BPlusTree<Object, ArrayList<DataRecord>> mTable;
    private ArrayList<DataRecord> mNullTable;
    private ArrayList<DataRecord> mAllRecords;
    private DataTypeIdentifier mIndexDataType;
    private int mKeyIndex;

    /**
     * Initialize a B Plus Tree Table.
     * Note that this constructor should only be called when restoring from disk.
     */
    public BPlusTreeTable() {
        super();
        mTable = new BPlusTree<>(100, 100);
        mNullTable = new ArrayList<>();
        mAllRecords = new ArrayList<>();
        mIndexDataType = DataTypeIdentifier.INT;
        mKeyIndex = 0;
    }

    /**
     * Initialize a B Plus Tree Table.
     *
     * @param tablename table name.
     * @param attributeNames a list of attribute names.
     * @param attributeTypes a list of attribute types.
     * @param primaryKey primary key column index.
     * @param keyIndex column index of this table, -1 if none.
     */
    public BPlusTreeTable(String tablename, ArrayList<String> attributeNames, ArrayList<DataType> attributeTypes, int primaryKey, int keyIndex) {
        super(tablename, attributeNames, attributeTypes, primaryKey);
        mTable = new BPlusTree<>(100, 100);
        mNullTable = new ArrayList<>();
        mAllRecords = new ArrayList<>();
        if (keyIndex == -1) {
            keyIndex = (primaryKey == -1) ? 0 : primaryKey;
        }
        mKeyIndex = keyIndex;
        mIndexDataType = attributeTypes.get(mKeyIndex).getType();
    }

    /**
     * Compare DataRecords according to key field.
     *
     * @param o1 data to compare.
     * @param o2 data to compare.
     * @return same as Comparable.compareTo().<br>
     *         >0: o1 > o2<br>
     *         =0: o1 = o2<br>
     *         <0: o1 < o2<br>
     */
    private int compareKeyField(DataRecord o1, DataRecord o2) {
        Object o1Key = o1.get(mKeyIndex);
        Object o2Key = o2.get(mKeyIndex);
        if (mIndexDataType == DataTypeIdentifier.INT) {
            return ((Integer) o1Key).compareTo((Integer) o2Key);
        } else {
            return ((String) o1Key).compareTo((String) o2Key);
        }
    }

    /**
     * Check whether data record given already exists or not.
     *
     * @param dataRecord data to check.
     * @return true if exists, false otherwise.
     */
    private boolean isDuplicatedData(DataRecord dataRecord) {
        ArrayList<DataRecord> results = mTable.get(dataRecord.get(mKeyIndex));
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
                table.insertAll(getAllRecords());
                return table;
            } else {
                return super.queryNotEqual(columnIndex, key);
            }
        } else {
            return super.queryNotEqual(columnIndex, key);
        }
    }

    @Override
    public Table queryLess(int columnIndex, Object key) {
        Table table = generateEmptyResultTable();
        if (key == null) {
            return table;
        }
        if (mKeyIndex == columnIndex) {
            ArrayList<ArrayList<DataRecord>> records = mTable.getValuesLess(key);
            for (ArrayList<DataRecord> recordArrayList : records) {
                table.insertAll(recordArrayList);
            }
            return table;
        } else {
            return super.queryLess(columnIndex, key);
        }
    }

    @Override
    public Table queryLessEqual(int columnIndex, Object key) {
        Table table = generateEmptyResultTable();
        if (key == null) {
            return table;
        }
        if (mKeyIndex == columnIndex) {
            ArrayList<ArrayList<DataRecord>> records = mTable.getValuesLessEqual(key);
            for (ArrayList<DataRecord> recordArrayList : records) {
                table.insertAll(recordArrayList);
            }
            return table;
        } else {
            return super.queryLessEqual(columnIndex, key);
        }
    }

    @Override
    public Table queryGreater(int columnIndex, Object key) {
        Table table = generateEmptyResultTable();
        if (key == null) {
            return table;
        }
        if (mKeyIndex == columnIndex) {
            ArrayList<ArrayList<DataRecord>> records = mTable.getValuesGreater(key);
            for (ArrayList<DataRecord> recordArrayList : records) {
                table.insertAll(recordArrayList);
            }
            return table;
        } else {
            return super.queryGreater(columnIndex, key);
        }
    }

    @Override
    public Table queryGreaterEqual(int columnIndex, Object key) {
        Table table = generateEmptyResultTable();
        if (key == null) {
            return table;
        }
        if (mKeyIndex == columnIndex) {
            ArrayList<ArrayList<DataRecord>> records = mTable.getValuesGreaterEqual(key);
            for (ArrayList<DataRecord> recordArrayList : records) {
                table.insertAll(recordArrayList);
            }
            return table;
        } else {
            return super.queryGreaterEqual(columnIndex, key);
        }
    }

    @Override
    public Table queryRange(int columnIndex, Object fromKey, Object toKey) {
        Table table = generateEmptyResultTable();
        if (fromKey == null || toKey == null) {
            return table;
        }
        if (mKeyIndex == columnIndex) {
            ArrayList<ArrayList<DataRecord>> records = mTable.getValues(fromKey, toKey);
            for (ArrayList<DataRecord> recordArrayList : records) {
                table.insertAll(recordArrayList);
            }
            return table;
        } else {
            return super.queryRange(columnIndex, fromKey, toKey);
        }
    }

    @Override
    public Table queryRange(int columnIndex, Object fromKey, boolean fromInclusive, Object toKey, boolean toInclusive) {
        Table table = generateEmptyResultTable();
        if (fromKey == null || toKey == null) {
            return table;
        }
        if (mKeyIndex == columnIndex) {
            ArrayList<ArrayList<DataRecord>> records = mTable.getValues(fromKey, fromInclusive, toKey, toInclusive);
            for (ArrayList<DataRecord> recordArrayList : records) {
                table.insertAll(recordArrayList);
            }
            return table;
        } else {
            return super.queryRange(columnIndex, fromKey, fromInclusive, toKey, toInclusive);
        }
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
        notNullDataRecords.sort(this::compareKeyField);
        ArrayList<Object> sortedKeys = new ArrayList<>();
        ArrayList<ArrayList<DataRecord>> sortedDatas = new ArrayList<>();
        sortedKeys.add(notNullDataRecords.get(0).get(mKeyIndex));
        sortedDatas.add(new ArrayList<>());
        sortedDatas.get(0).add(notNullDataRecords.get(0));
        DataRecord last = notNullDataRecords.get(0);
        int sIndex = 0;
        for (int i = 1; i < notNullDataRecords.size(); ++i) {
            if (compareKeyField(last, notNullDataRecords.get(i)) != 0) {
                ++sIndex;
                last = notNullDataRecords.get(i);
                sortedKeys.add(notNullDataRecords.get(i).get(mKeyIndex));
                sortedDatas.add(new ArrayList<>());
            }
            sortedDatas.get(sIndex).add(notNullDataRecords.get(i));
        }
        mTable.construct(sortedKeys, sortedDatas);
        return true;
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
        BPlusTreeTable table = new BPlusTreeTable(aliasName, mAttributeNames, mAttributeTypes, mPrimaryKey, mKeyIndex);
        table.mTable = this.mTable;
        table.mNullTable = this.mNullTable;
        table.mAllRecords = this.mAllRecords;
        return table;
    }

    @Override
    public String getTableType() {
        return "BPLUSTREE";
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

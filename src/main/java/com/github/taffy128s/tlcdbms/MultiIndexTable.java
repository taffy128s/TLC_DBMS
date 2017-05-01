package com.github.taffy128s.tlcdbms;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Multi-index table.
 * Each key can be indexed by tree, hash or not indexed.
 */
public class MultiIndexTable extends Table {
    private ArrayList<TableStructure> mIndices;
    private ArrayList<Table> mTables;
    private Table mFirstTable;
    private int mKeyIndex;

    /**
     * Initialize a Multi-index Table.
     * Note that this constructor should only be called when restoring from disk.
     */
    public MultiIndexTable() {
        super();
        mIndices = new ArrayList<>();
        mTables = new ArrayList<>();
    }

    /**
     * Initialize a set table with attribute names, types and
     * primary key (-1 if no primary key).
     *
     * @param attrNames an array list of names.
     * @param attrTypes an array list of types.
     * @param attrIndices an array list of index types.
     * @param primaryKey primary key index, -1 if none.
     * @param keyIndex column index of this table, -1 if none.
     */
    public MultiIndexTable(String tablename, ArrayList<String> attrNames, ArrayList<DataType> attrTypes, ArrayList<TableStructure> attrIndices, int primaryKey, int keyIndex) {
        super(tablename, attrNames, attrTypes, primaryKey);
        mIndices = attrIndices;
        mTables = new ArrayList<>();
        if (keyIndex == -1) {
            keyIndex = (primaryKey == -1) ? 0 : primaryKey;
        }
        mKeyIndex = keyIndex;
        for (int i = 0; i < attrNames.size(); ++i) {
            mTables.add(null);
        }
        for (TableStructure tableStructure : attrIndices) {
            if (tableStructure.getType() == TableStructType.BPLUSTREE) {
                BPlusTreeTable newTable = new BPlusTreeTable(tablename, attrNames, attrTypes, primaryKey, tableStructure.getIndex());
                mTables.set(tableStructure.getIndex(), newTable);
            } else if (tableStructure.getType() == TableStructType.HASH) {
                HashTable newTable = new HashTable(tablename, attrNames, attrTypes, primaryKey, tableStructure.getIndex());
                mTables.set(tableStructure.getIndex(), newTable);
            }
        }
        for (Table table : mTables) {
            if (table != null) {
                mFirstTable = table;
            }
        }
    }

    @Override
    public InsertionResult checkInputData(DataRecord dataRecord) {
        for (Table table : mTables) {
            if (table != null) {
                InsertionResult singleResult = table.checkInputData(dataRecord);
                if (singleResult != InsertionResult.SUCCESS) {
                    return singleResult;
                }
            }
        }
        return InsertionResult.SUCCESS;
    }

    @Override
    public boolean insert(DataRecord dataRecord) {
        boolean result = true;
        for (Table table : mTables) {
            if (table != null) {
                result &= table.insert(dataRecord);
            }
        }
        return result;
    }

    @Override
    protected boolean insertAll(ArrayList<DataRecord> dataRecords) {
        boolean result = true;
        for (TableStructure tableStructure : mIndices) {
            result &= mTables.get(tableStructure.getIndex()).insertAll(dataRecords);
        }
        return result;
    }

    @Override
    public Table queryEqual(int columnIndex, Object key) {
        if (mTables.get(columnIndex) != null) {
            return mTables.get(columnIndex).queryEqual(columnIndex, key);
        } else {
            return mFirstTable.queryEqual(columnIndex, key);
        }
    }

    @Override
    public Table queryNotEqual(int columnIndex, Object key) {
        if (mTables.get(columnIndex) != null) {
            return mTables.get(columnIndex).queryNotEqual(columnIndex, key);
        } else {
            return mFirstTable.queryNotEqual(columnIndex, key);
        }
    }

    @Override
    public Table queryLess(int columnIndex, Object key) {
        if (mTables.get(columnIndex) != null) {
            return mTables.get(columnIndex).queryLess(columnIndex, key);
        } else {
            return mFirstTable.queryLess(columnIndex, key);
        }
    }

    @Override
    public Table queryLessEqual(int columnIndex, Object key) {
        if (mTables.get(columnIndex) != null) {
            return mTables.get(columnIndex).queryLessEqual(columnIndex, key);
        } else {
            return mFirstTable.queryLessEqual(columnIndex, key);
        }
    }

    @Override
    public Table queryGreater(int columnIndex, Object key) {
        if (mTables.get(columnIndex) != null) {
            return mTables.get(columnIndex).queryGreater(columnIndex, key);
        } else {
            return mFirstTable.queryGreater(columnIndex, key);
        }
    }

    @Override
    public Table queryGreaterEqual(int columnIndex, Object key) {
        if (mTables.get(columnIndex) != null) {
            return mTables.get(columnIndex).queryGreaterEqual(columnIndex, key);
        } else {
            return mFirstTable.queryGreaterEqual(columnIndex, key);
        }
    }

    @Override
    public Table queryRange(int columnIndex, Object fromKey, Object toKey) {
        if (mTables.get(columnIndex) != null) {
            return mTables.get(columnIndex).queryRange(columnIndex, fromKey, toKey);
        } else {
            return mFirstTable.queryRange(columnIndex, fromKey, toKey);
        }
    }

    @Override
    public Table queryRange(int columnIndex, Object fromKey, boolean fromInclusive, Object toKey, boolean toInclusive) {
        if (mTables.get(columnIndex) != null) {
            return mTables.get(columnIndex).queryRange(columnIndex, fromKey, fromInclusive, toKey, toInclusive);
        } else {
            return mFirstTable.queryRange(columnIndex, fromKey, fromInclusive, toKey, toInclusive);
        }
    }

    @Override
    public ArrayList<DataRecord> getAllRecords() {
        return mTables.get(mIndices.get(0).getIndex()).getAllRecords();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<DataRecord> getAllRecords(int sortIndex, SortingType sortingType) {
        if (mTables.get(sortIndex) != null) {
            ArrayList<DataRecord> allRecords = mTables.get(sortIndex).getAllRecords(sortIndex, sortingType);
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
        MultiIndexTable table = new MultiIndexTable(aliasName, mAttributeNames, mAttributeTypes, mIndices, mPrimaryKey, mKeyIndex);
        table.mTables = new ArrayList<>();
        for (int i = 0; i < mTables.size(); ++i) {
            if (mTables.get(i) != null) {
                table.mTables.add(mTables.get(i).generateAliasTable(aliasName));
            } else {
                table.mTables.add(null);
            }
        }
        for (int i = 0; i < table.mTables.size(); ++i) {
            if (table.mTables.get(i) != null) {
                table.mFirstTable = table.mTables.get(i);
                break;
            }
        }
        return table;
    }

    @Override
    public String getTableType() {
        return "MULTI";
    }

    @Override
    public TableFieldType getFieldType(int index) {
        if (index == mPrimaryKey) {
            return TableFieldType.PRIMARY_KEY;
        } else if (mTables.get(index) != null) {
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
            writer.write(mIndices.size() + "\n");
            for (TableStructure tableStructure : mIndices) {
                writer.write(tableStructure.getIndex() + "\0");
                if (tableStructure.getType() == TableStructType.BPLUSTREE) {
                    writer.write("BPLUSTREE\n");
                } else if (tableStructure.getType() == TableStructType.HASH) {
                    writer.write("HASH\n");
                }
            }
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
            for (int i = 0; i < attrSize; ++i) {
                mTables.add(null);
            }
            mSourceTables.add(mTablename);
            mPrimaryKey = Integer.parseInt(reader.readLine());
            int indicesSize = Integer.parseInt(reader.readLine());
            for (int i = 0; i < indicesSize; ++i) {
                String indexString = reader.readLine();
                String[] options = indexString.split("\0");
                int columnIndex = Integer.parseInt(options[0]);
                if (options[1].equalsIgnoreCase("bplustree")) {
                    mIndices.add(new TableStructure(columnIndex, TableStructType.BPLUSTREE));
                    mTables.set(columnIndex, new BPlusTreeTable(mTablename, mAttributeNames, mAttributeTypes, mPrimaryKey, columnIndex));
                } else if (options[1].equalsIgnoreCase("hash")) {
                    mIndices.add(new TableStructure(columnIndex, TableStructType.HASH));
                    mTables.set(columnIndex, new HashTable(mTablename, mAttributeNames, mAttributeTypes, mPrimaryKey, columnIndex));
                }
            }
            ArrayList<DataRecord> records = new ArrayList<>();
            while ((input = reader.readLine()) != null) {
                DataRecord record = new DataRecord();
                record.restoreFromString(input);
                records.add(record);
            }
            insertAll(records);
            for (Table table : mTables) {
                if (table != null) {
                    mFirstTable = table;
                    break;
                }
            }
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

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
    public ArrayList<DataRecord> getAllRecords() {
        return mTables.get(mIndices.get(0).getIndex()).getAllRecords();
    }

    @Override
    public ArrayList<DataRecord> getAllRecords(int sortIndex, SortingType sortingType) {
        if (mTables.get(sortIndex) != null) {
            ArrayList<DataRecord> allRecords = mTables.get(sortIndex).getAllRecords(sortIndex, sortingType);
            if (sortingType == SortingType.DESCENDING) {
                Collections.reverse(allRecords);
            }
            return allRecords;
        } else {
            ArrayList<DataRecord> allRecords = getAllRecords();
            ArrayList<DataRecord> nullRecords = new ArrayList<>();
            ArrayList<DataRecord> notNullRecords = new ArrayList<>();
            for (DataRecord record : allRecords) {
                if (record.get(sortIndex) == null) {
                    nullRecords.add(record);
                } else {
                    notNullRecords.add(record);
                }
            }
            final int coefficient = (sortingType == SortingType.ASCENDING) ? 1 : -1;
            notNullRecords.sort((o1, o2) -> coefficient * ((Comparable) o1.get(sortIndex)).compareTo(o2.get(sortIndex)));
            allRecords.clear();
            if (coefficient == 1) {
                allRecords.addAll(nullRecords);
                allRecords.addAll(notNullRecords);
            } else {
                allRecords.addAll(notNullRecords);
                allRecords.addAll(nullRecords);
            }
            return allRecords;
        }
    }

    @Override
    public String getTableType() {
        return "MULTIINDEXTABLE";
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
            for (int i = 0; i < attrSize; ++i) {
                mTables.add(null);
            }
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

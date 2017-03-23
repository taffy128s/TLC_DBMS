package com.github.taffy128s.tlcdbms;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Database Table.
 *
 * It contains all operations related to the table in DB,
 * including CREATE, INSERT, SELECT...
 */
public abstract class Table implements DiskWritable {
    public static final String AUTO_PRIMARY_KEY_NAME = "$__AUTO_PRIMARY_KEY__";

    protected String mTablename;
    protected ArrayList<String> mAttributeNames;
    protected ArrayList<DataType> mAttributeTypes;
    protected HashSet<Integer> mAutoPrimaryKeyTable;
    protected int mPrimaryKey;
    protected int mAutoPrimaryKeyCounter;

    /**
     * Initialize a Table.
     * Note that this constructor should only be called when restoring from disk.
     */
    public Table() {
        mTablename = "";
        mAttributeNames = new ArrayList<>();
        mAttributeTypes = new ArrayList<>();
        mAutoPrimaryKeyTable = new HashSet<>();
        mPrimaryKey = -1;
        mAutoPrimaryKeyCounter = 0;
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
    }

    /**
     * Set auto primary key value.
     * Index = dataRecord.length() - 1, i.e. the last one.
     *
     * @param dataRecord data record to set.
     */
    protected void setAutoPrimaryKey(DataRecord dataRecord) {
        while (mAutoPrimaryKeyTable.contains(mAutoPrimaryKeyCounter)) {
            ++mAutoPrimaryKeyCounter;
        }
        mAutoPrimaryKeyTable.add(mAutoPrimaryKeyCounter);
        dataRecord.set(dataRecord.length() - 1, mAutoPrimaryKeyCounter);
    }

    /**
     * Tablename setter.
     *
     * @param tablename table name to set.
     */
    public void setTablename(String tablename) {
        mTablename = tablename;
    }

    /**
     * Tablename getter.
     *
     * @return table name.
     */
    public String getTablename() {
        return mTablename;
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
     * Get table type,
     * such as "SETTABLE", "TREETABLE".
     *
     * @return a string of table type.
     */
    public abstract String getTableType();

    /**
     * Get field key type.
     * NORMAL(not key), KEY, PRIMARY_KEY.
     *
     * @param index index to get.
     * @return key type.
     */
    public abstract TableFieldType getFieldType(int index);

    /**
     * Check whether input data record is a valid data tuple in this table.
     *
     * @param dataRecord data to check.
     * @return check result.
     */
    public abstract InsertionResult checkInputData(DataRecord dataRecord);

    /**
     * Insert a data record into table.
     * Assume that data is valid, i.e.<br>
     * <code>checkInputData(dataRecord) == InsertionResult.SUCCESS</code>
     *
     * @param dataRecord data record to be inserted.
     * @return true if succeed, false if failed.
     */
    public abstract boolean insert(DataRecord dataRecord);

    /**
     * Insert all data records into table.
     * Assume that all data are with valid type.
     * Used for disk restoreFromDisk().
     *
     * @param dataRecords a list of data records.
     * @return true if succeed, false if failed.
     */
    protected abstract boolean insertAll(ArrayList<DataRecord> dataRecords);

    /**
     * Get all data which has the same value as key of specified column index.
     * (i.e. = ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return an array list of DataRecords as result.
     */
    public ArrayList<DataRecord> query(int columnIndex, Object key) {
        ArrayList<DataRecord> result = new ArrayList<>();
        ArrayList<DataRecord> allRecords = getAllRecords();
        if (key == null) {
            for (DataRecord record : allRecords) {
                if (record.get(columnIndex) == null) {
                    result.add(record);
                }
            }
            return result;
        }
        for (DataRecord record : allRecords) {
            if (key.equals(record.get(columnIndex))) {
                result.add(record);
            }
        }
        return result;
    }

    /**
     * Get all data which has less value than key of specified column index.
     * (i.e. < ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return an array list of DataRecords as result.
     */
    public ArrayList<DataRecord> queryLess(int columnIndex, Object key) {
        if (key == null) {
            return new ArrayList<>();
        }
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : allRecords) {
            if (record.get(columnIndex) != null && ((Comparable) record.get(columnIndex)).compareTo(key) < 0) {
                result.add(record);
            }
        }
        return result;
    }

    /**
     * Get all data which has less or equal value than key of specified column index.
     * (i.e. <= ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return an array list of DataRecords as result.
     */
    public ArrayList<DataRecord> queryLessEqual(int columnIndex, Object key) {
        if (key == null) {
            return new ArrayList<>();
        }
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : allRecords) {
            if (record.get(columnIndex) != null && ((Comparable) record.get(columnIndex)).compareTo(key) <= 0) {
                result.add(record);
            }
        }
        return result;
    }

    /**
     * Get all data which has greater value than key of specified column index.
     * (i.e. > ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return an array list of DataRecords as result.
     */
    public ArrayList<DataRecord> queryGreater(int columnIndex, Object key) {
        if (key == null) {
            return new ArrayList<>();
        }
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : allRecords) {
            if (record.get(columnIndex) != null && ((Comparable) record.get(columnIndex)).compareTo(key) > 0) {
                result.add(record);
            }
        }
        return result;
    }

    /**
     * Get all data which has greater or equal value than key of specified column index.
     * (i.e. >= ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return an array list of DataRecords as result.
     */
    public ArrayList<DataRecord> queryGreaterEqual(int columnIndex, Object key) {
        if (key == null) {
            return new ArrayList<>();
        }
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : allRecords) {
            if (record.get(columnIndex) != null && ((Comparable) record.get(columnIndex)).compareTo(key) >= 0) {
                result.add(record);
            }
        }
        return result;
    }

    /**
     * Get all data which the key of specified column index is in range [fromKey, toKey).
     *
     * @param columnIndex column (or field) index to check.
     * @param fromKey key to start. (lower bound).
     * @param toKey key to end. (upper bound).
     * @return an array list of DataRecords as result.
     */
    public ArrayList<DataRecord> queryRange(int columnIndex, Object fromKey, Object toKey) {
        if (fromKey == null || toKey == null) {
            return new ArrayList<>();
        }
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : allRecords) {
            if (record.get(columnIndex) == null) {
                continue;
            }
            if (((Comparable) record.get(columnIndex)).compareTo(fromKey) >= 0 &&
                        ((Comparable) record.get(columnIndex)).compareTo(toKey) < 0) {
                result.add(record);
            }
        }
        return result;
    }

    /**
     * Get all data which the key of specified column index is in range from fromKey to toKey.
     * Two boolean parameter to specify whether fromKey and toKey is inclusive or not.
     *
     * @param columnIndex column (or field) index to check.
     * @param fromKey key to start. (lower bound).
     * @param fromInclusive whether fromKey is inclusive or not.
     * @param toKey key to end. (upper bound).
     * @param toInclusive whether toKey is inclusive or not.
     * @return an array list of DataRecords as result.
     */
    public ArrayList<DataRecord> queryRange(int columnIndex, Object fromKey, boolean fromInclusive, Object toKey, boolean toInclusive) {
        if (fromKey == null || toKey == null) {
            return new ArrayList<>();
        }
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : allRecords) {
            if (record.get(columnIndex) == null) {
                continue;
            }
            int compareToFrom = ((Comparable) record.get(columnIndex)).compareTo(fromKey);
            int compareToTo = ((Comparable) record.get(columnIndex)).compareTo(toKey);
            if (fromInclusive) {
                if (compareToFrom < 0) {
                    continue;
                }
            } else {
                if (compareToFrom <= 0) {
                    continue;
                }
            }
            if (toInclusive) {
                if (compareToTo > 0) {
                    continue;
                }
            } else {
                if (compareToTo >= 0) {
                    continue;
                }
            }
            result.add(record);
        }
        return result;
    }

    /**
     * Get all records in the table.
     *
     * @return an array list of all records.
     */
    public abstract ArrayList<DataRecord> getAllRecords();

    /**
     * Get all records in the table.
     * Sorted by column index given in parameter.
     *
     * @param sortIndex column (field) index to sort.
     * @return an array list of all records.
     */
    public abstract ArrayList<DataRecord> getAllRecords(int sortIndex, SortingType sortingType);

    @Override
    public String toString() {
        return ("Table " + mTablename) + "\n";
    }

    @Override
    public abstract boolean writeToDisk(String filename);

    @Override
    public abstract boolean restoreFromDisk(String filename);
}

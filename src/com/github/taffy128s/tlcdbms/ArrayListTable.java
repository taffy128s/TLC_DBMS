package com.github.taffy128s.tlcdbms;

import java.util.ArrayList;

/**
 * Array list table.
 * This table is only used as return type of Table.query() functions
 * due to the speed of insert(), insertAll() and getAllRecords().
 * It shouldn't be used in normal case since this table has no error
 * handling such as duplicated primary key or duplicated data tuple
 * checking. That is, this table will accept any input data and directly
 * insert it at the end of the list it maintains.
 */
public class ArrayListTable extends Table {
    private ArrayList<DataRecord> mTable;

    public ArrayListTable() {
        super();
        mTable = new ArrayList<>();
    }

    public ArrayListTable(String tablename, ArrayList<String> attributeNames, ArrayList<DataType> attributeTypes, int primaryKey, int keyIndex) {
        super(tablename, attributeNames, attributeTypes, primaryKey);
        mTable = new ArrayList<>();
    }

    @Override
    public InsertionResult checkInputData(DataRecord dataRecord) {
        return InsertionResult.SUCCESS;
    }

    @Override
    public boolean insert(DataRecord dataRecord) {
        mTable.add(dataRecord);
        return true;
    }

    @Override
    protected boolean insertAll(ArrayList<DataRecord> dataRecords) {
        mTable.addAll(dataRecords);
        return true;
    }

    @Override
    public ArrayList<DataRecord> getAllRecords() {
        return mTable;
    }

    @Override
    public ArrayList<DataRecord> getAllRecords(int sortIndex, SortingType sortingType) {
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

    @Override
    public TableFieldType getFieldType(int index) {
        return TableFieldType.NORMAL;
    }

    @Override
    public String getTableType() {
        return "ARRAYLISTTABLE";
    }

    @Override
    public boolean writeToDisk(String filename) {
        return false;
    }

    @Override
    public boolean restoreFromDisk(String filename) {
        return false;
    }
}

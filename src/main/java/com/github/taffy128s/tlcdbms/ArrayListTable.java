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

    /**
     * Initialize an arraylist table.
     */
    public ArrayListTable() {
        super();
        mTable = new ArrayList<>();
    }

    /**
     * Initialize an arraylist table with attribute names, types and
     * primary key (-1 if no primary key).
     *
     * @param attributeNames an array list of names.
     * @param attributeTypes an array list of types.
     * @param primaryKey primary key index, -1 if none.
     * @param keyIndex column index of this table, -1 if none.
     */
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
    public Table generateAliasTable(String aliasName) {
        return this;
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

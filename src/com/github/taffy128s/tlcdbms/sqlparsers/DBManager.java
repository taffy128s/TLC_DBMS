package com.github.taffy128s.tlcdbms.sqlparsers;

import com.github.taffy128s.tlcdbms.DataType;
import com.github.taffy128s.tlcdbms.Table;

import java.util.ArrayList;
import java.util.HashMap;

public class DBManager {
    private HashMap<String, Table> mTables;

    public DBManager() {
        mTables = new HashMap<>();
    }

    public void createTable(SQLParseResult parameter) {
        String tablename = parameter.getTablename();
        ArrayList<String> attributeNames = parameter.getAttributeNames();
        ArrayList<DataType> attributeTypes = parameter.getAttributeTypes();
        int primaryKey = parameter.getPrimaryKeyIndex();
        if (mTables.containsKey(tablename)) {
            System.out.println("Table " + tablename + " already exists");
            return;
        }
        Table newTable = new Table(tablename, attributeNames, attributeTypes, primaryKey);
        mTables.put(tablename, newTable);
    }
}

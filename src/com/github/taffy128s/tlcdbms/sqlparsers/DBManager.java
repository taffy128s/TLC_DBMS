package com.github.taffy128s.tlcdbms.sqlparsers;

import com.github.taffy128s.tlcdbms.*;

import java.util.ArrayList;
import java.util.HashMap;

public class DBManager {
    private HashMap<String, Table> mTables;

    public DBManager() {
        mTables = new HashMap<>();
    }

    public void create(SQLParseResult parameter) {
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

    public void insert(SQLParseResult parameter) {
        String tablename = parameter.getTablename();
        if (!mTables.containsKey(tablename)) {
            System.out.println("Table " + tablename + " not exists");
            return;
        }
        DataRecord dataRecord = generateDataRecord(parameter);
        if (dataRecord == null) {
            return;
        }
        mTables.get(tablename).insert(dataRecord);
        System.out.println(mTables.get(tablename).toString());
    }

    private DataRecord generateDataRecord(SQLParseResult parameter) {
        Table table = mTables.get(parameter.getTablename());
        ArrayList<String> attributeNames = table.getAttributeNames();
        ArrayList<DataType> attributeTypes = table.getAttributeTypes();
        ArrayList<Integer> orderIndex = new ArrayList<>();
        if (parameter.getAttributeNames().size() != attributeNames.size()) {
            System.out.println("Input data size not match!");
            System.out.println("Found " + parameter.getAttributeNames().size());
            System.out.println("Expect " + attributeNames.size());
            return null;
        }
        if (parameter.getCustomOrder()) {
            for (String attrName : attributeNames) {
                int index = parameter.getAttributeNames().indexOf(attrName);
                if (index == -1) {
                    System.out.println("Attribute " + attrName + " not found in input data");
                    return null;
                }
                orderIndex.add(index);
            }
        } else {
            for (int i = 0; i < attributeNames.size(); ++i) {
                orderIndex.add(i);
            }
        }
        DataRecord dataRecord = new DataRecord();
        for (int index : orderIndex) {
            String block = parameter.getBlocks().get(index);
            if (attributeTypes.get(index).getType() == DataTypeIdentifier.INT) {
                if (!DataChecker.isValidInteger(block)) {
                    System.out.println("Error input type (INT expected): " + block);
                    return null;
                }
                dataRecord.append(block);
            } else {
                String varcharPart = block.substring(1, block.length() - 1);
                int lengthLimit = attributeTypes.get(index).getLimit();
                if (!DataChecker.isValidVarChar(varcharPart, lengthLimit)) {
                    System.out.println("Error input type (VARCHAR(" + lengthLimit + ") expected)" + varcharPart);
                    return null;
                }
                dataRecord.append(block);
            }
        }
        return dataRecord;
    }
}

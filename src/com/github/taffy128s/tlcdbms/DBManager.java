package com.github.taffy128s.tlcdbms;

import com.github.taffy128s.tlcdbms.sqlparsers.SQLParseResult;

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
    }

    public void show(SQLParseResult parameter) {
        String tablename = parameter.getTablename();
        if (!mTables.containsKey(tablename)) {
            System.out.println("Table " + tablename + " not exists");
            return;
        }
        ArrayList<String> attributeNames = mTables.get(tablename).getAttributeNames();
        ArrayList<DataType> attributeTypes = mTables.get(tablename).getAttributeTypes();
        Object[] allRecords = mTables.get(tablename).getAllRecords();
        printTable(attributeNames, allRecords);
    }

    public void desc(SQLParseResult parameter) {
        String tablename = parameter.getTablename();
        if (!mTables.containsKey(tablename)) {
            System.out.println("Table " + tablename + " not exists");
            return;
        }
        ArrayList<String> attributeNames = mTables.get(tablename).getAttributeNames();
        ArrayList<DataType> attributeTypes = mTables.get(tablename).getAttributeTypes();
        ArrayList<String> showAttr = new ArrayList<>();
        showAttr.add("Name");
        showAttr.add("Type");
        DataRecord[] datas = new DataRecord[attributeNames.size()];
        for (int i = 0; i < attributeNames.size(); ++i) {
            datas[i] = new DataRecord();
            datas[i].append(attributeNames.get(i));
            datas[i].append(attributeTypes.get(i));
        }
        printTable(showAttr, datas);
    }

    private DataRecord generateDataRecord(SQLParseResult parameter) {
        Table table = mTables.get(parameter.getTablename());
        ArrayList<String> attributeNames = table.getAttributeNames();
        ArrayList<DataType> attributeTypes = table.getAttributeTypes();
        ArrayList<Integer> orderIndex = new ArrayList<>();
        if (parameter.getBlocks().size() != attributeNames.size()) {
            System.out.println("Input data size not match!");
            System.out.println("Found " + parameter.getBlocks().size());
            System.out.println("Expect " + attributeNames.size());
            return null;
        }
        if (parameter.isCustomOrder()) {
            for (String attrName : attributeNames) {
                int index = parameter.getUpdateOrder().indexOf(attrName);
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
        int tableAttrIndex = 0;
        for (int index : orderIndex) {
            String block = parameter.getBlocks().get(index);
            if (attributeTypes.get(tableAttrIndex).getType() == DataTypeIdentifier.INT) {
                if (!DataChecker.isValidInteger(block)) {
                    System.out.println("Error input type (INT expected): " + block);
                    return null;
                }
                dataRecord.append(block);
            } else {
                int lengthLimit = attributeTypes.get(tableAttrIndex).getLimit();
                if (!DataChecker.isValidQuotedVarChar(block)) {
                    System.out.println("Error input type (VARCHAR(" + lengthLimit + ") expected):" + block);
                    return null;
                }
                String varcharPart = block.substring(1, block.length() - 1);
                if (!DataChecker.isValidVarChar(varcharPart, lengthLimit)) {
                    System.out.println("Error input type (VARCHAR(" + lengthLimit + ") expected):" + varcharPart);
                    return null;
                }
                dataRecord.append(block);
            }
            ++tableAttrIndex;
        }
        return dataRecord;
    }

    private void printTable(ArrayList<String> attribute, Object[] datas) {
        ArrayList<Integer> columnMaxLength = new ArrayList<>();
        for (String anAttribute : attribute) {
            columnMaxLength.add(anAttribute.length() + 2);
        }
        for (Object data : datas) {
            DataRecord record = (DataRecord) data;
            Object[] blocks = record.getAllFields();
            for (int i = 0; i < blocks.length; ++i) {
                columnMaxLength.set(i, Math.max(columnMaxLength.get(i), blocks[i].toString().length() + 2));
            }
        }
        String attrOutput = "";
        for (int i = 0; i < attribute.size(); ++i) {
            if (i > 0) {
                attrOutput += " |";
            } else {
                attrOutput += "  ";
            }
            for (int j = 0; j < columnMaxLength.get(i) - attribute.get(i).length(); ++j) {
                attrOutput += " ";
            }
            attrOutput += attribute.get(i);
        }
        System.out.println(attrOutput);
        for (int i = 0; i < attrOutput.length(); ++i) {
            if (attrOutput.charAt(i) == '|') {
                System.out.print("+");
            } else {
                System.out.print("-");
            }
        }
        System.out.println();
        for (Object data : datas) {
            DataRecord record = (DataRecord) data;
            Object[] blocks = record.getAllFields();
            for (int i = 0; i < blocks.length; ++i) {
                if (i > 0) {
                    System.out.print(" |");
                } else {
                    System.out.print("  ");
                }
                for (int j = 0; j < columnMaxLength.get(i) - blocks[i].toString().length(); ++j) {
                    System.out.print(" ");
                }
                System.out.print(blocks[i]);
            }
            System.out.println();
        }
    }
}

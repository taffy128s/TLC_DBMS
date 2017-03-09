package com.github.taffy128s.tlcdbms;

import com.github.taffy128s.tlcdbms.sqlparsers.SQLParseResult;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Database manager.
 * Check the result generated by parser, and pass it to table(if valid).
 */
public class DBManager {
    private HashMap<String, Table> mTables;

    /**
     * Initialize.
     */
    public DBManager() {
        mTables = new HashMap<>();
    }

    /**
     * Do CREATE.
     *
     * @param parameter parse result generated by parser.
     */
    public void create(SQLParseResult parameter) {
        String tablename = parameter.getTablename();
        ArrayList<String> attributeNames = parameter.getAttributeNames();
        ArrayList<DataType> attributeTypes = parameter.getAttributeTypes();
        int primaryKey = parameter.getPrimaryKeyIndex();
        if (mTables.containsKey(tablename)) {
            System.out.println("Table '" + tablename + "' already exists.");
            return;
        }
        Table newTable = new Table(tablename, attributeNames, attributeTypes, primaryKey);
        mTables.put(tablename, newTable);
        System.out.println("Table '" + tablename + "' created successfully.");
    }

    /**
     * Do INSERT.
     *
     * @param parameter parse result generated by parser.
     */
    public void insert(SQLParseResult parameter) {
        String tablename = parameter.getTablename();
        if (!mTables.containsKey(tablename)) {
            System.out.println("Table '" + tablename + "' not exists.");
            return;
        }
        DataRecord dataRecord = generateDataRecord(parameter);
        if (dataRecord == null) {
            return;
        }
        boolean result = mTables.get(tablename).insert(dataRecord);
        if (result) {
            System.out.println("Table '" + tablename + "': 1 row added.");
        }
    }

    /**
     * Do SELECT.
     *
     * @param parameter parse result generated by parser.
     */
    public void select(SQLParseResult parameter) {
        // next stage.
    }

    /**
     * Do SHOW table lists.
     *
     * @param parameter parse result generated by parser.
     */
    public void showTableList(SQLParseResult parameter) {
        ArrayList<String> showAttr = new ArrayList<>();
        ArrayList<DataType> showType = new ArrayList<>();
        showAttr.add("Tablename");
        showType.add(new DataType(DataTypeIdentifier.VARCHAR, 40));
        Object[] tablenames = mTables.keySet().toArray();
        DataRecord[] records = new DataRecord[mTables.size()];
        for (int i = 0; i < records.length; ++i) {
            records[i] = new DataRecord();
            records[i].append(tablenames[i].toString());
        }
        printTable(showAttr, showType, records);
    }

    /**
     * Do SHOW table content.
     *
     * @param parameter parse result generated by parser.
     */
    public void showTableContent(SQLParseResult parameter) {
        String tablename = parameter.getTablename();
        if (!mTables.containsKey(tablename)) {
            System.out.println("Table '" + tablename + "' not exists.");
            return;
        }
        ArrayList<String> attributeNames = mTables.get(tablename).getAttributeNames();
        ArrayList<DataType> attributeTypes = mTables.get(tablename).getAttributeTypes();
        Object[] allRecords = mTables.get(tablename).getAllRecords();
        printTable(attributeNames, attributeTypes, allRecords);
    }

    /**
     * Do DESC.
     *
     * @param parameter parse result generated by parser.
     */
    public void desc(SQLParseResult parameter) {
        String tablename = parameter.getTablename();
        if (!mTables.containsKey(tablename)) {
            System.out.println("Table '" + tablename + "' not exists.");
            return;
        }
        ArrayList<String> attributeNames = mTables.get(tablename).getAttributeNames();
        ArrayList<DataType> attributeTypes = mTables.get(tablename).getAttributeTypes();
        ArrayList<String> showAttr = new ArrayList<>();
        ArrayList<DataType> showType = new ArrayList<>();
        showAttr.add("Name");
        showAttr.add("Type");
        showType.add(new DataType(DataTypeIdentifier.INT, -1));
        showType.add(new DataType(DataTypeIdentifier.VARCHAR, 40));
        DataRecord[] datas = new DataRecord[attributeNames.size()];
        for (int i = 0; i < attributeNames.size(); ++i) {
            datas[i] = new DataRecord();
            datas[i].append(attributeNames.get(i));
            datas[i].append(attributeTypes.get(i));
        }
        printTable(showAttr, showType, datas);
    }

    /**
     * Generate a DataRecord with given data.
     *
     * @param parameter parse result generated by parser.
     * @return a DataRecord if succeed, null if failed.
     */
    private DataRecord generateDataRecord(SQLParseResult parameter) {
        Table table = mTables.get(parameter.getTablename());
        ArrayList<String> attributeNames = table.getAttributeNames();
        ArrayList<DataType> attributeTypes = table.getAttributeTypes();
        ArrayList<Integer> orderIndex = new ArrayList<>();
        if (parameter.isCustomOrder()) {
            for (String attrName : attributeNames) {
                int index = parameter.getUpdateOrder().indexOf(attrName);
                orderIndex.add(index);
            }
            for (String attrName : parameter.getUpdateOrder()) {
                int index = attributeNames.indexOf(attrName);
                if (index == -1) {
                    System.out.println("Attribute '" + attrName + "' not found in table '" + parameter.getTablename() + "'.");
                    return null;
                }
            }
        } else {
            for (int i = 0; i < attributeNames.size(); ++i) {
                orderIndex.add(i);
            }
        }
        DataRecord dataRecord = new DataRecord();
        int tableAttrIndex = 0;
        for (int index : orderIndex) {
            String block = (index != -1) ? parameter.getBlocks().get(index) : null;
            if (block == null) {
                dataRecord.append(null);
            } else if (attributeTypes.get(tableAttrIndex).getType() == DataTypeIdentifier.INT) {
                if (!DataChecker.isValidInteger(block)) {
                    System.out.println("Wrong input type (INT expected): '" + block + "'.");
                    return null;
                }
                dataRecord.append(block);
            } else {
                int lengthLimit = attributeTypes.get(tableAttrIndex).getLimit();
                if (!DataChecker.isValidQuotedVarChar(block)) {
                    System.out.println("Wrong input type (VARCHAR(" + lengthLimit + ") expected): '" + block + "'.");
                    return null;
                }
                String varcharPart = block.substring(1, block.length() - 1);
                if (!DataChecker.isValidVarChar(varcharPart, lengthLimit)) {
                    System.out.println("Wrong input type (VARCHAR(" + lengthLimit + ") expected): '" + varcharPart + "'.");
                    return null;
                }
                dataRecord.append(block);
            }
            ++tableAttrIndex;
        }
        return dataRecord;
    }

    /**
     * Print a table (output related).
     *
     * @param attribute attribute names.
     * @param type attribute types.
     * @param datas data to print.
     */
    private void printTable(ArrayList<String> attribute, ArrayList<DataType> type, Object[] datas) {
        System.out.println();
        ArrayList<Integer> columnMaxLength = new ArrayList<>();
        for (String anAttribute : attribute) {
            columnMaxLength.add(anAttribute.length() + 1);
        }
        for (Object data : datas) {
            DataRecord record = (DataRecord) data;
            Object[] blocks = record.getAllFieldsForOutput();
            for (int i = 0; i < blocks.length; ++i) {
                columnMaxLength.set(i, Math.max(columnMaxLength.get(i), blocks[i].toString().length() + 1));
            }
        }
        String attrOutput = "";
        for (int i = 0; i < attribute.size(); ++i) {
            attrOutput += " |";
            for (int j = 0; j < columnMaxLength.get(i) - attribute.get(i).length(); ++j) {
                attrOutput += " ";
            }
            attrOutput += attribute.get(i);
        }
        attrOutput += " |";
        printSeparateLine(attrOutput);
        System.out.println(attrOutput);
        printSeparateLine(attrOutput);
        for (Object data : datas) {
            DataRecord record = (DataRecord) data;
            Object[] blocks = record.getAllFieldsForOutput();
            for (int i = 0; i < blocks.length; ++i) {
                System.out.print(" |");
                if (type.get(i).getType() == DataTypeIdentifier.INT) {
                    for (int j = 0; j < columnMaxLength.get(i) - blocks[i].toString().length(); ++j) {
                        System.out.print(" ");
                    }
                } else {
                    System.out.print(" ");
                }
                System.out.print(blocks[i]);
                if (type.get(i).getType() == DataTypeIdentifier.VARCHAR) {
                    for (int j = 0; j < columnMaxLength.get(i) - blocks[i].toString().length() - 1; ++j) {
                        System.out.print(" ");
                    }
                }
            }
            System.out.print(" |");
            System.out.println();
        }
        printSeparateLine(attrOutput);
        System.out.println();
    }

    /**
     * Print separate line (like +----+-----+).
     *
     * @param attrOutput attribute output.
     */
    private void printSeparateLine(String attrOutput) {
        for (int i = 0; i < attrOutput.length(); ++i) {
            if (i < 1) {
                System.out.print(" ");
            } else if (attrOutput.charAt(i) == '|') {
                System.out.print("+");
            } else {
                System.out.print("-");
            }
        }
        System.out.println();
    }
}

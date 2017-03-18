package com.github.taffy128s.tlcdbms;

import com.github.taffy128s.tlcdbms.sqlparsers.SQLParseResult;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Database manager.
 * Check the result generated by parser, and pass it to table(if valid).
 */
public class DBManager implements DiskWritable {
    public static final String FILENAME = "dbtables.tlc";
    public static final String DIRNAME = "dbtlc";

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
        ArrayList<TableStructure> attributeIndices = parameter.getAttributeIndices();
        attributeNames.add(Table.AUTO_PRIMARY_KEY_NAME);
        attributeTypes.add(new DataType(DataTypeIdentifier.INT, -1));
        int primaryKey = parameter.getPrimaryKeyIndex();
        if (mTables.containsKey(tablename)) {
            System.out.println("Table '" + tablename + "' already exists.");
            return;
        }
        Table newTable;
        if (attributeIndices == null) {
            newTable = new SetTable(tablename, attributeNames, attributeTypes, primaryKey);
        } else {
            newTable = new MultiIndexTable(tablename, attributeNames, attributeTypes, attributeIndices, primaryKey);
        }
        mTables.put(tablename, newTable);
        System.out.println("Query OK, table '" + tablename + "' created successfully.");
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
        InsertionResult checkResult = mTables.get(tablename).checkInputData(dataRecord);
        if (checkResult == InsertionResult.DUPLICATED_DATA_TUPLE) {
            System.out.println("Data tuple already exists in table.");
        } else if (checkResult == InsertionResult.NULL_PRIMARY_KEY) {
            System.out.println("Primary Key field cannot be null.");
        } else if (checkResult == InsertionResult.DUPLICATED_PRIMARY_KEY) {
            int primaryKeyIndex = mTables.get(tablename).getPrimaryKey();
            System.out.println("Primary Key " + dataRecord.get(primaryKeyIndex) + " already exists in table.");
        } else {
            mTables.get(tablename).insert(dataRecord);
            System.out.println("Query OK, table '" + tablename + "': 1 row added.");
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
        ArrayList<DataRecord> records = new ArrayList<>();
        for (int i = 0; i < tablenames.length; ++i) {
            DataRecord record = new DataRecord();
            record.append(tablenames[i].toString());
            records.add(record);
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
        ArrayList<DataRecord> allRecords = mTables.get(tablename).getAllRecords();
        if (parameter.getShowRowLimitation() != -1) {
            allRecords.subList(parameter.getShowRowLimitation(), allRecords.size()).clear();
        }
        printTable(attributeNames, attributeTypes, allRecords, !parameter.getShowFullInfo());
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
        showAttr.add("Key");
        showType.add(new DataType(DataTypeIdentifier.VARCHAR, 40));
        showType.add(new DataType(DataTypeIdentifier.VARCHAR, 40));
        showType.add(new DataType(DataTypeIdentifier.VARCHAR, 40));
        ArrayList<DataRecord> records = new ArrayList<>();
        for (int i = 0; i < attributeNames.size(); ++i) {
            if (!parameter.getShowFullInfo() && attributeNames.get(i).equalsIgnoreCase(Table.AUTO_PRIMARY_KEY_NAME)) {
                continue;
            }
            DataRecord record = new DataRecord();
            record.append(attributeNames.get(i));
            record.append(attributeTypes.get(i));
            if (i == mTables.get(tablename).getPrimaryKey()) {
                record.append("PRI");
            } else if (attributeNames.get(i).equalsIgnoreCase(Table.AUTO_PRIMARY_KEY_NAME)) {
                record.append("AUTO_PRI");
            } else {
                record.append("");
            }
            records.add(record);
        }
        printTable(showAttr, showType, records);
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
        ArrayList<String> attributeStrings = new ArrayList<>();
        ArrayList<Integer> orderIndex = new ArrayList<>();
        for (int i = 0; i < attributeNames.size(); ++i) {
            if (attributeNames.get(i).equalsIgnoreCase(Table.AUTO_PRIMARY_KEY_NAME)) {
                continue;
            }
            if (i == table.getPrimaryKey()) {
                attributeStrings.add(attributeNames.get(i) + " " + attributeTypes.get(i) + " PRI");
            } else {
                attributeStrings.add(attributeNames.get(i) + " " + attributeTypes.get(i));
            }
        }
        parameter.getBlocks().add(null);
        int expectedSize = attributeNames.size() - 1;
        int gotSize = parameter.getBlocks().size() - 1;
        if (!parameter.isCustomOrder() && expectedSize != gotSize) {
            System.out.println("Input data tuple size doesn't match table attributes.");
            System.out.println("Table '" + parameter.getTablename() + "' attributes: " + String.join(", ", attributeStrings));
            System.out.println("Expected: " + expectedSize + ".");
            System.out.println("Given: " + gotSize + ".");
            return null;
        }
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
                    System.out.println("For attribute '" + attributeNames.get(tableAttrIndex) + "' in table '" + parameter.getTablename() + "':");
                    System.out.println("Wrong input type (INT expected): " + block + ".");
                    return null;
                }
                dataRecord.append(Integer.parseInt(block));
            } else {
                int lengthLimit = attributeTypes.get(tableAttrIndex).getLimit();
                if (!DataChecker.isValidQuotedVarChar(block)) {
                    System.out.println("For attribute '" + attributeNames.get(tableAttrIndex) + "' in table '" + parameter.getTablename() + "':");
                    System.out.println("Wrong input type (VARCHAR(" + lengthLimit + ") expected): " + block + ".");
                    return null;
                }
                String varcharPart = block.substring(1, block.length() - 1);
                if (!DataChecker.isValidVarChar(varcharPart, lengthLimit)) {
                    System.out.println("For attribute '" + attributeNames.get(tableAttrIndex) + "' in table '" + parameter.getTablename() + "':");
                    System.out.print("Wrong input type (VARCHAR(" + lengthLimit + ") expected): " + block);
                    System.out.println(" with length " + varcharPart.length() + ".");
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
     * Hide any attribute related to AUTO_PRIMARY_KEY.
     *
     * @param attribute attribute names.
     * @param type attribute types.
     * @param records data to print.
     */
    private void printTable(ArrayList<String> attribute, ArrayList<DataType> type, ArrayList<DataRecord> records) {
        printTable(attribute, type, records, true);
    }

    /**
     * Print a table (output related).
     *
     * @param attribute attribute names.
     * @param type attribute types.
     * @param records data to print.
     * @param hideAutoPrimaryKey true to hide auto generated primary key, false otherwise
     */
    private void printTable(ArrayList<String> attribute, ArrayList<DataType> type, ArrayList<DataRecord> records, boolean hideAutoPrimaryKey) {
        if (records.isEmpty()) {
            System.out.println(" (empty set.)");
            return;
        }
        boolean containAutoPrimaryKey = attribute.get(attribute.size() - 1).equalsIgnoreCase(Table.AUTO_PRIMARY_KEY_NAME);
        ArrayList<Integer> columnMaxLength = new ArrayList<>();
        for (String anAttribute : attribute) {
            if (hideAutoPrimaryKey && anAttribute.equalsIgnoreCase(Table.AUTO_PRIMARY_KEY_NAME)) {
                continue;
            }
            columnMaxLength.add(anAttribute.length() + 1);
        }
        for (DataRecord record : records) {
            ArrayList<Object> blocks = record.getAllFieldsForOutput();
            for (int i = 0; i < blocks.size(); ++i) {
                if (containAutoPrimaryKey && hideAutoPrimaryKey && i == blocks.size() - 1) {
                    continue;
                }
                columnMaxLength.set(i, Math.max(columnMaxLength.get(i), blocks.get(i).toString().length() + 1));
            }
        }
        String attrOutput = "";
        for (int i = 0; i < attribute.size(); ++i) {
            if (hideAutoPrimaryKey && attribute.get(i).equalsIgnoreCase(Table.AUTO_PRIMARY_KEY_NAME)) {
                continue;
            }
            attrOutput += " | ";
            attrOutput += attribute.get(i);
            for (int j = 0; j < columnMaxLength.get(i) - attribute.get(i).length() - 1; ++j) {
                attrOutput += " ";
            }
        }
        attrOutput += " |";
        printSeparateLine(attrOutput);
        System.out.println(attrOutput);
        printSeparateLine(attrOutput);
        for (DataRecord record : records) {
            ArrayList<Object> blocks = record.getAllFieldsForOutput();
            for (int i = 0; i < blocks.size(); ++i) {
                if (containAutoPrimaryKey && hideAutoPrimaryKey && i == blocks.size() - 1) {
                    continue;
                }
                System.out.print(" |");
                if (type.get(i).getType() == DataTypeIdentifier.INT) {
                    for (int j = 0; j < columnMaxLength.get(i) - blocks.get(i).toString().length(); ++j) {
                        System.out.print(" ");
                    }
                } else {
                    System.out.print(" ");
                }
                System.out.print(blocks.get(i));
                if (type.get(i).getType() == DataTypeIdentifier.VARCHAR) {
                    for (int j = 0; j < columnMaxLength.get(i) - blocks.get(i).toString().length() - 1; ++j) {
                        System.out.print(" ");
                    }
                }
            }
            System.out.print(" |");
            System.out.println();
        }
        printSeparateLine(attrOutput);
        if (records.size() == 1) {
            System.out.println(" (" + records.size() + " row in set.)");
        } else {
            System.out.println(" (" + records.size() + " rows in set.)");
        }
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

    @Override
    public boolean writeToDisk(String filename) {
        File parentDirectory = new File(DIRNAME);
        if (!parentDirectory.exists()) {
            boolean result = parentDirectory.mkdir();
            if (!result) {
                System.err.println("./" + DIRNAME + "/: creation error.");
                return false;
            }
        }
        filename = "./" + DIRNAME + "/" + filename;
        try {
            FileWriter writer = new FileWriter(filename);
            ArrayList<String> tablenames = new ArrayList<>();
            for (String tablename : mTables.keySet()) {
                writer.write(tablename + "\0");
                writer.write(mTables.get(tablename).getTableType() + "\n");
                tablenames.add(tablename);
            }
            for (String tablename : tablenames) {
                mTables.get(tablename).writeToDisk("./" + DIRNAME + "/" + tablename + ".tlctable");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println(filename + ": file I/O error.");
        }
        return false;
    }

    @Override
    public boolean restoreFromDisk(String filename) {
        File dbFile = new File("./" + DIRNAME + "/" + filename);
        if (!dbFile.exists()) {
            return true;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./" + DIRNAME + "/" + filename));
            String input;
            while ((input = reader.readLine()) != null) {
                String[] tableAttr = input.split("\0");
                if (tableAttr[1].equalsIgnoreCase("SETTABLE")) {
                    Table setTable = new SetTable();
                    setTable.restoreFromDisk("./" + DIRNAME + "/" + tableAttr[0] + ".tlctable");
                    mTables.put(tableAttr[0], setTable);
                } else if (tableAttr[1].equalsIgnoreCase("MULTIINDEXTABLE")) {
                    Table multiIndexTable = new MultiIndexTable();
                    multiIndexTable.restoreFromDisk("./" + DIRNAME + "/" + tableAttr[0] + ".tlctable");
                    mTables.put(tableAttr[0], multiIndexTable);
                } else {
                    System.err.println("Unsupported table type.");
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("./" + DIRNAME + "/" + filename + ": no such file or directory.");
        } catch (IOException e) {
            System.err.println("./" + DIRNAME + "/" + filename + ": file I/O error.");
        }
        return false;
    }
}

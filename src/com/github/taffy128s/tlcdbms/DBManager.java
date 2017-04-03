package com.github.taffy128s.tlcdbms;

import com.github.taffy128s.tlcdbms.sqlparsers.SQLParseResult;
import java.io.*;
import java.util.*;

/**
 * Database manager.
 * Check the result generated by parser, and pass it to table(if valid).
 */
public class DBManager implements DiskWritable {
    /**
     * Null output stream. Output will be ignored.
     */
    private static class NullOutputStream extends PrintStream {
        /**
         * Default constructor.
         */
        public NullOutputStream() {
            super(new OutputStream() {
                @Override
                public void write(int b) throws IOException {

                }
            });
        }
    }

    public static final String FILENAME = "dbtables.tlc";
    public static final String DIRNAME = "dbtlc";

    private HashMap<String, Table> mTables;
    private HashMap<String, Table> mQueryTables;

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
        int primaryKey = parameter.getPrimaryKeyIndex();
        if (mTables.containsKey(tablename)) {
            System.out.println("Table '" + tablename + "' already exists.");
            return;
        }
        Table newTable;
        if (attributeIndices == null) {
            newTable = new SetTable(tablename, attributeNames, attributeTypes, primaryKey, -1);
        } else {
            newTable = new MultiIndexTable(tablename, attributeNames, attributeTypes, attributeIndices, primaryKey, -1);
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
        mQueryTables = new HashMap<>();
        String randomTablename = "";
        HashMap<String, String> aliasMap = parameter.getTableAliases();
        for (String tableName : parameter.getTablenames()) {
        	if (!mTables.containsKey(tableName)) {
        		System.out.println("Table '" + tableName + "' doesn't exist.");
        		return;
        	}
        }
        for (String alias : aliasMap.keySet()) {
            if (randomTablename.equalsIgnoreCase("")) {
                randomTablename = alias;
            }
            mQueryTables.put(alias, mTables.get(aliasMap.get(alias)).generateAliasTable(alias));
        }
        if (parameter.getConditions() == null) {
            parameter.setConditions(new ArrayList<>());
        }
        for (Condition condition : parameter.getConditions()) {
            if (condition.getOperator() == BinaryOperator.AND || condition.getOperator() == BinaryOperator.OR) {
                continue;
            }
            if (!setConditionParameters(condition, parameter)) {
                return;
            }
        }
        if (!setTargetsParameters(parameter)) {
            return;
        }
        if (!setSortingTarget(parameter)) {
            return;
        }
        Stack<Table> selectedTables = new Stack<>();
        if (parameter.getConditions().isEmpty()) {
            selectedTables.push(mQueryTables.get(randomTablename).query(Condition.getAlwaysTrueCondition()));
        } else {
            for (Condition condition : parameter.getConditions()) {
                if (condition.getOperator() == BinaryOperator.AND) {
                    Table second = selectedTables.pop();
                    Table first = selectedTables.pop();
                    selectedTables.push(Table.intersect(first, second, mQueryTables));
                } else if (condition.getOperator() == BinaryOperator.OR) {
                    Table second = selectedTables.pop();
                    Table first = selectedTables.pop();
                    selectedTables.push(Table.union(first, second, mQueryTables));
                } else if (condition.getLeftConstant() != null && condition.getRightConstant() != null) {
                    selectedTables.push(mQueryTables.get(randomTablename).query(condition));
                } else if (condition.getLeftConstant() != null && condition.getRightConstant() == null) {
                    selectedTables.push(mQueryTables.get(condition.getRightTableName()).query(condition));
                } else if (condition.getLeftConstant() == null && condition.getRightConstant() != null) {
                    selectedTables.push(mQueryTables.get(condition.getLeftTableName()).query(condition));
                } else if (condition.getLeftConstant() == null && condition.getRightConstant() == null) {
                    if (!condition.getLeftTableName().equals(condition.getRightTableName())) {
                        selectedTables.push(Table.join(mQueryTables.get(condition.getLeftTableName()), mQueryTables.get(condition.getRightTableName()), condition));
                    } else {
                        selectedTables.push(mQueryTables.get(condition.getLeftTableName()).query(condition));
                    }
                }
            }
        }
        Table resultTable = selectedTables.pop();
        for (String tablename : aliasMap.keySet()) {
            if (!resultTable.getSourceTables().contains(tablename)) {
                resultTable = Table.join(resultTable, mQueryTables.get(tablename), Condition.getAlwaysTrueCondition());
            }
        }
        if (parameter.getQueryType() == QueryType.COUNT) {
            String queryTargetString = "COUNT(";
            if (parameter.getTargets().get(0).getTableName() != null) {
                queryTargetString += parameter.getTargets().get(0).getTableName() + ".";
            }
            queryTargetString += parameter.getTargets().get(0).getAttribute() + ")";
            int answer = resultTable.getAllRecords().size();
            ArrayList<String> attributes = new ArrayList<>();
            ArrayList<DataType> types = new ArrayList<>();
            ArrayList<DataRecord> records = new ArrayList<>();
            DataRecord record = new DataRecord();
            record.append(answer);
            attributes.add(queryTargetString);
            types.add(new DataType(DataTypeIdentifier.INT, -1));
            records.add(record);
            printTable(attributes, types, records);
        } else if (parameter.getQueryType() == QueryType.SUM) {
            String queryTargetString = "SUM(";
            if (parameter.getTargets().get(0).getTableName() != null) {
                queryTargetString += parameter.getTargets().get(0).getTableName() + ".";
            }
            queryTargetString += parameter.getTargets().get(0).getAttribute() + ")";
            String target = parameter.getTargets().get(0).getTableName() + "." + parameter.getTargets().get(0).getAttribute();
            int index = resultTable.getAttributeNames().indexOf(target);
            ArrayList<DataRecord> allRecords = resultTable.getAllRecords();
            int answer = 0;
            for (DataRecord record : allRecords) {
                answer += (Integer) record.get(index);
            }
            ArrayList<String> attributes = new ArrayList<>();
            ArrayList<DataType> types = new ArrayList<>();
            ArrayList<DataRecord> records = new ArrayList<>();
            DataRecord record = new DataRecord();
            record.append(answer);
            attributes.add(queryTargetString);
            types.add(new DataType(DataTypeIdentifier.INT, -1));
            records.add(record);
            printTable(attributes, types, records);
        } else {
            ArrayList<Integer> targetIndices = new ArrayList<>();
            ArrayList<DataRecord> allRecords;
            if (parameter.getAttributeNames() != null) {
                String target = parameter.getAttributeNames().get(0);
                int sortIndex = resultTable.getAttributeNames().indexOf(target);
                allRecords = resultTable.getAllRecords(sortIndex, parameter.getShowSortType());
            } else {
                allRecords = resultTable.getAllRecords();
            }
            if (parameter.getShowRowLimitation() != -1) {
                int startIndex = Math.min(parameter.getShowRowLimitation(), allRecords.size());
                allRecords.subList(startIndex, allRecords.size()).clear();
            }
            for (int i = 0; i < parameter.getTargets().size(); ++i) {
                Target target = parameter.getTargets().get(i);
                if (target.getAttribute().equals("*")) {
                    if (target.getTableName() == null) {
                        printTable(resultTable.getAttributeNames(), resultTable.getAttributeTypes(), allRecords);
                        return;
                    } else {
                        String tablename = target.getTableName();
                        for (int j = 0; j < resultTable.getAttributeNames().size(); ++j) {
                            if (resultTable.getAttributeNames().get(j).startsWith(tablename)) {
                                targetIndices.add(j);
                            }
                        }
                    }
                } else {
                    String tablename = target.getTableName();
                    String attrName = target.getAttribute();
                    String targetAttr = tablename + "." + attrName;
                    targetIndices.add(resultTable.getAttributeNames().indexOf(targetAttr));
                }
            }
            ArrayList<String> attributes = new ArrayList<>();
            ArrayList<DataType> types = new ArrayList<>();
            ArrayList<DataRecord> records = new ArrayList<>();
            for (int index : targetIndices) {
                attributes.add(resultTable.getAttributeNames().get(index));
                types.add(resultTable.getAttributeTypes().get(index));
            }
            for (DataRecord record : allRecords) {
                DataRecord newRecord = new DataRecord();
                for (int index : targetIndices) {
                    newRecord.append(record.get(index));
                }
                records.add(newRecord);
            }
            printTable(attributes,types, records);
        }
    }

    /**
     * Do DROP.
     *
     * @param parameter parse result generated by parser.
     */
    public void drop(SQLParseResult parameter) {
        ArrayList<String> tablenames = parameter.getTablenames();
        if (tablenames.get(0) == null) {
            Set<String> keys = mTables.keySet();
            int counts = 0;
            for (String key : keys) {
                File tableFile = new File("./" + DIRNAME + "/" + key + ".tlctable");
                if (tableFile.exists()) {
                    if (!tableFile.delete()) {
                        System.err.println("Error occurred when deleting table file " + key);
                    }
                }
                System.out.println("Table '" + key + "' dropped.");
                ++counts;
                mTables.remove(key);
            }
            System.out.println("Query OK, " + counts + " rows affected.");
        } else {
            int counts = 0;
            for (String tablename : tablenames) {
                if (!mTables.containsKey(tablename)) {
                    System.out.println("Table '" + tablename + "' not exists.");
                } else {
                    File tableFile = new File("./" + DIRNAME + "/" + tablename + ".tlctable");
                    if (tableFile.exists()) {
                        if (!tableFile.delete()) {
                            System.err.println("Error occurred when deleting table file " + tablename);
                        }
                    }
                    System.out.println("Table '" + tablename + "' dropped.");
                    ++counts;
                    mTables.remove(tablename);
                }
            }
            System.out.println("Query OK, " + counts + " rows affected.");
        }
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
        ArrayList<DataRecord> allRecords;
        if (parameter.getAttributeNames() != null) {
            String sortAttributeName = parameter.getAttributeNames().get(0);
            final int sortIndex = attributeNames.indexOf(sortAttributeName);
            if (sortIndex == -1) {
                System.out.println("Attribute " + sortAttributeName + " not exists in table " + tablename);
                return;
            }
            allRecords = mTables.get(tablename).getAllRecords(sortIndex, parameter.getShowSortType());
        } else {
            allRecords = mTables.get(tablename).getAllRecords();
        }
        if (parameter.getShowRowLimitation() != -1) {
            int startIndex = Math.min(parameter.getShowRowLimitation(), allRecords.size());
            allRecords.subList(startIndex, allRecords.size()).clear();
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
            DataRecord record = new DataRecord();
            record.append(attributeNames.get(i));
            record.append(attributeTypes.get(i));
            TableFieldType fieldType = mTables.get(tablename).getFieldType(i);
            if (fieldType == TableFieldType.PRIMARY_KEY) {
                record.append("PRI");
            } else if (fieldType == TableFieldType.KEY) {
                record.append("KEY");
            } else {
                record.append("");
            }
            records.add(record);
        }
        printTable(showAttr, showType, records);
    }

    /**
     * Do LOAD.
     *
     * @param parameter parse result generated by parser.
     */
    public void load(SQLParseResult parameter) {
        PrintStream originStdout = System.out;
        if (!parameter.getShowFullInfo()) {
            System.setOut(new NullOutputStream());
        }
        FileInterpreter fileInterpreter = new FileInterpreter(parameter.getFilename(), this);
        fileInterpreter.start();
        System.setOut(originStdout);
        System.out.println("Script file '" + parameter.getFilename() + "' loaded successfully.");
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
            if (i == table.getPrimaryKey()) {
                attributeStrings.add(attributeNames.get(i) + " " + attributeTypes.get(i) + " PRI");
            } else {
                attributeStrings.add(attributeNames.get(i) + " " + attributeTypes.get(i));
            }
        }
        int expectedSize = attributeNames.size();
        int gotSize = parameter.getBlocks().size();
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
     * Fill all fields in conditions such as tablenames.
     *
     * @param condition condition to process.
     * @param parameter parse result generated by parser.
     * @return true if succeed, false if failed.
     */
    private boolean setConditionParameters(Condition condition, SQLParseResult parameter) {
        DataTypeIdentifier leftType = null;
        DataTypeIdentifier rightType = null;
        boolean leftIsNull = false;
        boolean rightIsNull = false;
        if (condition.getLeftConstant() == null) {
            if (condition.getLeftTableName() == null) {
                int found = -1;
                for (String tableName : parameter.getTableAliases().keySet()) {
                    int index = mQueryTables.get(tableName).getAttributeNames().indexOf(condition.getLeftAttribute());
                    if (index != -1 && found != -1) {
                        System.out.println("Attribute '" + condition.getLeftAttribute() + "' is ambiguous.");
                        return false;
                    }
                    if (index != -1) {
                    	leftType = mQueryTables.get(tableName).getAttributeTypes().get(index).getType();
                        condition.setLeftTableName(tableName);
                        found = index;
                    }
                }
                if (found == -1) {
                    System.out.println("Attribute '" + condition.getLeftAttribute() + "' don't exist in any tables.");
                    return false;
                }
            }
            else {
                if (!mQueryTables.containsKey(condition.getLeftTableName())) {
                    System.out.println("Table '" + condition.getLeftTableName() + "' doesn't exist.");
                    return false;
                }
                int index = mQueryTables.get(condition.getLeftTableName()).getAttributeNames().indexOf(condition.getLeftAttribute());
                if (index == -1) {
                    System.out.println("Attribute '" + condition.getLeftAttribute() + "' of Table " +
                                               condition.getLeftTableName() + " doesn't exist.");
                    return false;
                }
                leftType = mQueryTables.get(condition.getLeftTableName()).getAttributeTypes().get(index).getType();
            }
        }
        else {
            leftIsNull = DataChecker.isStringNull(condition.getLeftConstant());
            leftType = (DataChecker.isValidInteger(condition.getLeftConstant())) ? DataTypeIdentifier.INT : DataTypeIdentifier.VARCHAR;
        }
        if (condition.getRightConstant() == null) {
            if (condition.getRightTableName() == null) {
                int found = -1;
                for (String tableName : parameter.getTableAliases().keySet()) {
                    int index = mQueryTables.get(tableName).getAttributeNames().indexOf(condition.getRightAttribute());
                    if (index != -1 && found != -1) {
                        System.out.println("Attribute '" + condition.getRightAttribute() + "' is ambiguous.");
                        return false;
                    }
                    if (index != -1) {
                        rightType = mQueryTables.get(tableName).getAttributeTypes().get(index).getType();
                        condition.setRightTableName(tableName);
                        found = index;
                    }
                }
                if (found == -1) {
                    System.out.println("Attribute '" + condition.getRightAttribute() + "' don't exist in any tables.");
                    return false;
                }
            }
            else {
                if (!mQueryTables.containsKey(condition.getRightTableName())) {
                    System.out.println("Table '" + condition.getRightTableName() + "' doesn't exist.");
                    return false;
                }
                int index = mQueryTables.get(condition.getRightTableName()).getAttributeNames().indexOf(condition.getRightAttribute());
                if (index == -1) {
                    System.out.println("Attribute '" + condition.getRightAttribute() + "' of Table " +
                                               condition.getRightTableName() + " doesn't exist.");
                    return false;
                }
                rightType = mQueryTables.get(condition.getRightTableName()).getAttributeTypes().get(index).getType();
            }
        }
        else {
            rightIsNull = DataChecker.isStringNull(condition.getRightConstant());
            rightType = (DataChecker.isValidInteger(condition.getRightConstant())) ? DataTypeIdentifier.INT : DataTypeIdentifier.VARCHAR;
        }
        if (leftIsNull && !rightIsNull) {
            leftType = rightType;
        } else if (!leftIsNull && rightIsNull) {
            rightType = leftType;
        }
        if (leftType != rightType) {
            System.out.println("The types in the condition are different.");
            System.out.println("Left: " + (leftType == DataTypeIdentifier.INT ? "INT" : "VARCHAR"));
            System.out.println("Right: " + (rightType == DataTypeIdentifier.INT ? "INT" : "VARCHAR"));
            return false;
        }
        return true;
    }

    /**
     * Fill sorting target parameter.
     *
     * @param parameter parse result generated by parser.
     * @return true if succeed, false if failed.
     */
    private boolean setSortingTarget(SQLParseResult parameter) {
        if (parameter.getAttributeNames() == null) {
            return true;
        }
        String target = parameter.getAttributeNames().get(0);
        String[] split = target.split("\\.");
        String tablename = null;
        String attribute;
        if (split.length == 1) {
            attribute = split[0];
        } else {
            tablename = split[0];
            attribute = split[1];
        }
        if (tablename == null) {
            int found = -1;
            for (String table : parameter.getTableAliases().keySet()) {
                int index = mQueryTables.get(table).getAttributeNames().indexOf(attribute);
                if (index != -1 && found != -1) {
                    System.out.println("Sort attribute '" + target + "' is ambiguous.");
                    return false;
                }
                if (index != -1) {
                    found = index;
                    tablename = table;
                }
            }
        } else {
            if (!parameter.getTableAliases().containsKey(tablename)) {
                System.out.println("Table '" + tablename + "' not exists.");
                return false;
            }
            if (mQueryTables.get(tablename).getAttributeNames().indexOf(attribute) == -1) {
                System.out.println("Sort Attribute '" + attribute + "' not exists in table " + tablename);
                return false;
            }
        }
        parameter.getAttributeNames().set(0, tablename + "." + attribute);
        return true;
    }

    /**
     * Fill all fields in targets such as tablenames.
     *
     * @param parameter parse result generated by parser.
     * @return true if succeed, false if failed.
     */
    private boolean setTargetsParameters(SQLParseResult parameter) {
        boolean selectAll = false;
        boolean selectPart = false;
        for (Target target : parameter.getTargets()) {
            if (target.getTableName() == null) {
                boolean found = false;
                if (target.getAttribute().equals("*")) {
                    if (selectAll || selectPart) {
                        System.out.println("Duplicate * query.");
                        return false;
                    }
                    selectAll = true;
                    continue;
                }
                for (String tableName : parameter.getTableAliases().keySet()) {
                    if (mQueryTables.get(tableName).getAttributeNames().contains(target.getAttribute())) {
                        if (found) {
                            System.out.println("Attribute '" + target.getAttribute() + "' is ambiguous.");
                            return false;
                        }
                        target.setTableName(tableName);
                        found = true;
                    }
                }
            }
            else {
                if (target.getAttribute().equals("*")) {
                	if (selectAll) {
                        System.out.println("Duplicate * query.");
                        return false;
                    }
                	selectPart = true;
                    continue;
                }
                if (!mQueryTables.containsKey(target.getTableName())) {
                    System.out.println("Table '" + target.getTableName() + "' doesn't exist.");
                    return false;
                }
                if (!mQueryTables.get(target.getTableName()).getAttributeNames().contains(target.getAttribute())) {
                    System.out.println("Attribute '" + target.getAttribute() + "' of Table " + target.getTableName() + " doesn't exist.");
                    return false;
                }
            }
        }
        return true;
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
        ArrayList<Integer> columnMaxLength = new ArrayList<>();
        for (String anAttribute : attribute) {
            columnMaxLength.add(anAttribute.length() + 1);
        }
        for (DataRecord record : records) {
            ArrayList<Object> blocks = record.getAllFieldsForOutput();
            for (int i = 0; i < blocks.size(); ++i) {
                columnMaxLength.set(i, Math.max(columnMaxLength.get(i), blocks.get(i).toString().length() + 1));
            }
        }
        String attrOutput = "";
        for (int i = 0; i < attribute.size(); ++i) {
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

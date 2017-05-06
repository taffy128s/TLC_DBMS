package com.github.taffy128s.tlcdbms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Database Table.
 *
 * It contains all operations related to the table in DB,
 * including CREATE, INSERT, SELECT...
 */
public abstract class Table implements DiskWritable {
    protected String mTablename;
    protected ArrayList<String> mAttributeNames;
    protected ArrayList<DataType> mAttributeTypes;
    protected int mPrimaryKey;

    protected ArrayList<String> mSourceTables;

    protected String mFilename;
    protected boolean mDiskModifiable;

    /**
     * Initialize a Table.
     * Note that this constructor should only be called when restoring from disk.
     */
    public Table() {
        mTablename = "";
        mAttributeNames = new ArrayList<>();
        mAttributeTypes = new ArrayList<>();
        mPrimaryKey = -1;
        mSourceTables = new ArrayList<>();
        mFilename = "";
        mDiskModifiable = false;
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
        mSourceTables.add(mTablename);
        mFilename = "./" + DBManager.DIRNAME + "/" + mTablename + ".tlctable";
        mDiskModifiable = false;
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
     * Get source tablenames.
     *
     * @return a list of all source tablenames.
     */
    public ArrayList<String> getSourceTables() {
        return mSourceTables;
    }

    /**
     * Get table type,
     * such as "HASH", "MULTI".
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
     * Get all data which satisfy the condition given in parameter.
     * Note that the condition should be set correctly in DBManager.
     *
     * @param condition condition as filter.
     * @return a table include all DataRecords as result.
     */
    public Table query(Condition condition) {
        boolean isResultTable = mTablename.equalsIgnoreCase("$result");
        if (condition.getLeftConstant() != null && condition.getRightConstant() != null) {
            Object left = Condition.getConstant(condition.getLeftConstant());
            Object right = Condition.getConstant(condition.getRightConstant());
            boolean result = Condition.calculateResult(left, right, condition.getOperator());
            Table table = generateEmptyResultTable();
            if (result) {
                table.insertAll(getAllRecords());
                return table;
            } else {
                return table;
            }
        } else if (condition.getLeftConstant() != null && condition.getRightConstant() == null) {
            String target = condition.getRightAttribute();
            if (isResultTable) {
                target = condition.getRightTableName() + "." + target;
            }
            int columnIndex = mAttributeNames.indexOf(target);
            Object right = Condition.getConstant(condition.getLeftConstant());
            BinaryOperator operator = Condition.reverseOperator(condition.getOperator());
            return query(columnIndex, right, operator);
        } else if (condition.getLeftConstant() == null && condition.getRightConstant() != null) {
            String target = condition.getLeftAttribute();
            if (isResultTable) {
                target = condition.getLeftTableName() + "." + target;
            }
            int columnIndex = mAttributeNames.indexOf(target);
            Object right = Condition.getConstant(condition.getRightConstant());
            BinaryOperator operator = condition.getOperator();
            return query(columnIndex, right, operator);
        } else {
            Table table = generateEmptyResultTable();
            if (!condition.getLeftTableName().equals(condition.getRightTableName())) {
                return table;
            }
            String leftTarget = condition.getLeftAttribute();
            String rightTarget = condition.getRightAttribute();
            if (isResultTable) {
                leftTarget = condition.getLeftTableName() + "." + leftTarget;
                rightTarget = condition.getRightTableName() + "." + rightTarget;
            }
            ArrayList<DataRecord> allRecords = getAllRecords();
            int leftIndex = mAttributeNames.indexOf(leftTarget);
            int rightIndex = mAttributeNames.indexOf(rightTarget);
            if (leftIndex == -1 || rightIndex == -1) {
                return table;
            }
            for (DataRecord record : allRecords) {
                if (Condition.calculateResult(record.get(leftIndex), record.get(rightIndex), condition.getOperator())) {
                    table.insert(record);
                }
            }
            return table;
        }
    }

    /**
     * Get all data which satisfy the condition given in parameter.
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to be compared.
     * @param operator a binary operator (like EQUAL).
     * @return a table with all DataRecords as result.
     */
    private Table query(int columnIndex, Object key, BinaryOperator operator) {
        switch (operator) {
            case EQUAL:
                return queryEqual(columnIndex, key);
            case NOT_EQUAL:
                return queryNotEqual(columnIndex, key);
            case LESS_THAN:
                return queryLess(columnIndex, key);
            case LESS_EQUAL:
                return queryLessEqual(columnIndex, key);
            case GREATER_THAN:
                return queryGreater(columnIndex, key);
            case GREATER_EQUAL:
                return queryGreaterEqual(columnIndex, key);
        }
        return generateEmptyResultTable();
    }

    /**
     * Get all data which has the same value as key of specified column index.
     * (i.e. = ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return a table with all DataRecords as result.
     */
    public Table queryEqual(int columnIndex, Object key) {
        ArrayList<DataRecord> result = new ArrayList<>();
        ArrayList<DataRecord> allRecords = getAllRecords();
        Table table = generateEmptyResultTable();
        if (key == null) {
            for (DataRecord record : allRecords) {
                if (record.get(columnIndex) == null) {
                    result.add(record);
                }
            }
            table.insertAll(result);
            return table;
        }
        for (DataRecord record : allRecords) {
            if (key.equals(record.get(columnIndex))) {
                result.add(record);
            }
        }
        table.insertAll(result);
        return table;
    }

    /**
     * Get all data which has different value as key of specified column index.
     * (i.e. = ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return a table with all DataRecords as result.
     */
    public Table queryNotEqual(int columnIndex, Object key) {
        ArrayList<DataRecord> result = new ArrayList<>();
        ArrayList<DataRecord> allRecords = getAllRecords();
        Table table = generateEmptyResultTable();
        if (key == null) {
            for (DataRecord record : allRecords) {
                if (record.get(columnIndex) != null) {
                    result.add(record);
                }
            }
            table.insertAll(result);
            return table;
        }
        for (DataRecord record : allRecords) {
            if (!key.equals(record.get(columnIndex))) {
                result.add(record);
            }
        }
        table.insertAll(result);
        return table;
    }

    /**
     * Get all data which has less value than key of specified column index.
     * (i.e. < ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return a table with all DataRecords as result.
     */
    @SuppressWarnings("unchecked")
    public Table queryLess(int columnIndex, Object key) {
        Table table = generateEmptyResultTable();
        if (key == null) {
            return table;
        }
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : allRecords) {
            if (record.get(columnIndex) != null && ((Comparable) record.get(columnIndex)).compareTo(key) < 0) {
                result.add(record);
            }
        }
        table.insertAll(result);
        return table;
    }

    /**
     * Get all data which has less or equal value than key of specified column index.
     * (i.e. <= ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return a table with all DataRecords as result.
     */
    @SuppressWarnings("unchecked")
    public Table queryLessEqual(int columnIndex, Object key) {
        Table table = generateEmptyResultTable();
        if (key == null) {
            return table;
        }
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : allRecords) {
            if (record.get(columnIndex) != null && ((Comparable) record.get(columnIndex)).compareTo(key) <= 0) {
                result.add(record);
            }
        }
        table.insertAll(result);
        return table;
    }

    /**
     * Get all data which has greater value than key of specified column index.
     * (i.e. > ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return a table with all DataRecords as result.
     */
    @SuppressWarnings("unchecked")
    public Table queryGreater(int columnIndex, Object key) {
        Table table = generateEmptyResultTable();
        if (key == null) {
            return table;
        }
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : allRecords) {
            if (record.get(columnIndex) != null && ((Comparable) record.get(columnIndex)).compareTo(key) > 0) {
                result.add(record);
            }
        }
        table.insertAll(result);
        return table;
    }

    /**
     * Get all data which has greater or equal value than key of specified column index.
     * (i.e. >= ).
     *
     * @param columnIndex column (or field) index to check.
     * @param key key to get.
     * @return a table with all DataRecords as result.
     */
    @SuppressWarnings("unchecked")
    public Table queryGreaterEqual(int columnIndex, Object key) {
        Table table = generateEmptyResultTable();
        if (key == null) {
            return table;
        }
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : allRecords) {
            if (record.get(columnIndex) != null && ((Comparable) record.get(columnIndex)).compareTo(key) >= 0) {
                result.add(record);
            }
        }
        table.insertAll(result);
        return table;
    }

    /**
     * Get all data which the key of specified column index is in range [fromKey, toKey).
     *
     * @param columnIndex column (or field) index to check.
     * @param fromKey key to start. (lower bound).
     * @param toKey key to end. (upper bound).
     * @return a table with all DataRecords as result.
     */
    @SuppressWarnings("unchecked")
    public Table queryRange(int columnIndex, Object fromKey, Object toKey) {
        Table table = generateEmptyResultTable();
        if (fromKey == null || toKey == null) {
            return table;
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
        table.insertAll(result);
        return table;
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
     * @return a table with all DataRecords as result.
     */
    @SuppressWarnings("unchecked")
    public Table queryRange(int columnIndex, Object fromKey, boolean fromInclusive, Object toKey, boolean toInclusive) {
        Table table = generateEmptyResultTable();
        if (fromKey == null || toKey == null) {
            return table;
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
        table.insertAll(result);
        return table;
    }

    /**
     * Generate a new table with all the same contents except tablename.
     * Used to generate an alias table.
     *
     * @param aliasName alias name.
     * @return an exactly the same table with alias name.
     */
    public abstract Table generateAliasTable(String aliasName);

    /**
     * Generate an empty table to be used as return table.
     * Set its tablename to "$result", and add tablename prefix
     * to all of its attribute names if necessary.
     *
     * @return an empty table to be used as result.
     */
    protected Table generateEmptyResultTable() {
        ArrayList<String> attributes = new ArrayList<>();
        ArrayList<DataType> types = new ArrayList<>();
        for (String attr : mAttributeNames) {
            if (mTablename.equalsIgnoreCase("$result")) {
                attributes.add(attr);
            } else {
                attributes.add(mTablename + "." + attr);
            }
        }
        for (DataType type : mAttributeTypes) {
            types.add(type);
        }
        Table result = new ArrayListTable("$result", attributes, types, -1, -1);
        result.mSourceTables = new ArrayList<>();
        result.mSourceTables.add(mTablename);
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
     * @param sortingType ascending or descending.
     * @return an array list of all records.
     */
    public ArrayList<DataRecord> getAllRecords(int sortIndex, SortingType sortingType) {
        ArrayList<DataRecord> allRecords = getAllRecords();
        ArrayList<Integer> sortIndices = new ArrayList<>();
        sortIndices.add(sortIndex);
        final int coefficient = (sortingType == SortingType.ASCENDING) ? 1 : -1;
        allRecords.sort((o1, o2) -> coefficient * DataRecord.compare(o1, o2, sortIndices));
        return allRecords;
    }

    /**
     * Get all records in the table.
     * Sorted by column indices given in parameter.
     *
     * @param sortIndices column (field) indices to sort.
     * @param sortingType ascending or descending.
     * @return an array list of all records.
     */
    public ArrayList<DataRecord> getAllRecords(ArrayList<Integer> sortIndices, SortingType sortingType) {
        if (sortIndices.size() == 1) {
            return getAllRecords(sortIndices.get(0), sortingType);
        } else {
            ArrayList<DataRecord> allRecords = getAllRecords();
            final int coefficient = (sortingType == SortingType.ASCENDING) ? 1 : -1;
            allRecords.sort((o1, o2) -> coefficient * DataRecord.compare(o1, o2, sortIndices));
            return allRecords;
        }
    }

    /**
     * Get all records in the table.
     * Sorted by column indices given in parameter.
     * Note that the size of sortIndices should equals to the size of sortingTypes.
     *
     * @param sortIndices column (field) indices to sort.
     * @param sortingTypes a list of ascending or descending for each column index.
     * @return an array list of all records.
     */
    public ArrayList<DataRecord> getAllRecords(ArrayList<Integer> sortIndices, ArrayList<SortingType> sortingTypes) {
        if (sortIndices.size() == 1) {
            return getAllRecords(sortIndices.get(0), sortingTypes.get(0));
        } else {
            ArrayList<DataRecord> allRecords = getAllRecords();
            allRecords.sort((o1, o2) -> DataRecord.compare(o1, o2, sortIndices, sortingTypes));
            return allRecords;
        }
    }

    /**
     * Join two table given in parameter. Condition in parameter should be set correctly,
     * that is, left value and right value should not be null, and tablename of them should
     * be equals to firstTable and secondTable, respectively. Wrong condition will get an
     * empty table.<br>
     * Note that properties of result table:<br>
     *     tablename: result<br>
     *     attribute names: name of firstTable append name of secondTable<br>
     *     attribute types: type of firstTable append type of secondTable<br>
     *     primary key: -1 (i.e. not exists)<br>
     *     key index: -1 (i.e. not exists)<br>
     *
     * @param firstTable a table to join.
     * @param secondTable another table to join.
     * @param condition join condition.
     * @return a table as result of joining two tables.
     */
    public static Table join(Table firstTable, Table secondTable, Condition condition) {
        ArrayList<String> newAttrNames = new ArrayList<>();
        ArrayList<DataType> newAttrTypes = new ArrayList<>();
        newAttrNames.addAll(firstTable.getAttributeNames());
        newAttrNames.addAll(secondTable.getAttributeNames());
        newAttrTypes.addAll(firstTable.getAttributeTypes());
        newAttrTypes.addAll(secondTable.getAttributeTypes());
        if (!firstTable.mTablename.equalsIgnoreCase("$result")) {
            for (int i = 0; i < firstTable.getAttributeNames().size(); ++i) {
                newAttrNames.set(i, firstTable.getTablename() + "." + newAttrNames.get(i));
            }
        }
        if (!secondTable.mTablename.equalsIgnoreCase("$result")) {
            for (int i = firstTable.getAttributeNames().size(); i < newAttrNames.size(); ++i) {
                newAttrNames.set(i, secondTable.getTablename() + "." + newAttrNames.get(i));
            }
        }
        Table table = new ArrayListTable("$result", newAttrNames, newAttrTypes, -1, -1);
        int leftKeyIndex = firstTable.getAttributeNames().indexOf(condition.getLeftAttribute());
        int rightKeyIndex = secondTable.getAttributeNames().indexOf(condition.getRightAttribute());
        if (Condition.getAlwaysTrueCondition().equals(condition)) {
            ArrayList<DataRecord> firstRecords = firstTable.getAllRecords();
            ArrayList<DataRecord> secondRecords = secondTable.getAllRecords();
            for (DataRecord firstRecord : firstRecords) {
                for (DataRecord secondRecord : secondRecords) {
                    DataRecord newRecord = new DataRecord();
                    newRecord.appendAll(firstRecord.getAllFields());
                    newRecord.appendAll(secondRecord.getAllFields());
                    table.insert(newRecord);
                }
            }
        } else {
            ArrayList<DataRecord> firstRecords = firstTable.getAllRecords(leftKeyIndex, SortingType.ASCENDING);
            Object lastKey = null;
            Table lastSubTable = null;
            for (DataRecord firstRecord : firstRecords) {
                Object key = firstRecord.get(leftKeyIndex);
                ArrayList<DataRecord> subRecords;
                if (key != null && key.equals(lastKey)) {
                    subRecords = lastSubTable.getAllRecords();
                } else {
                    lastKey = key;
                    lastSubTable = secondTable.query(rightKeyIndex, key, condition.getOperator());
                    subRecords = lastSubTable.getAllRecords();
                }
                for (DataRecord secondRecord : subRecords) {
                    DataRecord newRecord = new DataRecord();
                    newRecord.appendAll(firstRecord.getAllFields());
                    newRecord.appendAll(secondRecord.getAllFields());
                    table.insert(newRecord);
                }
            }
        }
        table.mSourceTables = new ArrayList<>();
        table.mSourceTables.addAll(firstTable.getSourceTables());
        table.mSourceTables.addAll(secondTable.getSourceTables());
        return table;
    }

    /**
     * Get union (OR) of two tables.
     * Set union.
     *
     * @param first first table.
     * @param second second table.
     * @return a table of result.
     */
    public static Table union(Table first, Table second, Map<String, Table> tables) {
        ArrayList<Table> joinResult = preProcessTables(first, second, tables);
        first = joinResult.get(0);
        second = joinResult.get(1);
        Table table = new ArrayListTable("$result", first.getAttributeNames(), first.getAttributeTypes(), -1, -1);
        ArrayList<DataRecord> records = first.getAllRecords();
        ArrayList<DataRecord> another = second.getAllRecords();
        ArrayList<Integer> indices = new ArrayList<>();
        for (String attr : second.getAttributeNames()) {
            indices.add(first.getAttributeNames().indexOf(attr));
        }
        HashSet<DataRecord> recordHashSet = new HashSet<>(records);
        for (DataRecord record : another) {
            DataRecord toCheck = new DataRecord();
            for (int index : indices) {
                toCheck.append(record.get(index));
            }
            recordHashSet.add(toCheck);
        }
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : recordHashSet) {
            result.add(record);
        }
        table.insertAll(result);
        table.mSourceTables = new ArrayList<>();
        table.mSourceTables.addAll(first.getSourceTables());
        return table;
    }

    /**
     * Get intersection (AND) of two tables.
     * Set intersection.
     *
     * @param first first table.
     * @param second second table.
     * @return a table of result.
     */
    public static Table intersect(Table first, Table second, Map<String, Table> tables) {
        if (first.getAttributeNames().containsAll(second.getAttributeNames()) ||
                second.getAttributeNames().containsAll(first.getAttributeNames())) {
            if (first.getAttributeNames().size() < second.getAttributeNames().size()) {
                Table temp = first;
                first = second;
                second = temp;
            }
        } else {
            ArrayList<Table> joinResult = preProcessTables(first, second, tables);
            first = joinResult.get(0);
            second = joinResult.get(1);
        }
        Table table = new ArrayListTable("$result", first.getAttributeNames(), first.getAttributeTypes(), -1, -1);
        HashSet<DataRecord> recordHashSet = new HashSet<>(second.getAllRecords());
        ArrayList<Integer> indices = new ArrayList<>();
        for (String attr : second.getAttributeNames()) {
            indices.add(first.getAttributeNames().indexOf(attr));
        }
        ArrayList<DataRecord> result = new ArrayList<>();
        for (DataRecord record : first.getAllRecords()) {
            DataRecord toCheck = new DataRecord();
            for (int index : indices) {
                toCheck.append(record.get(index));
            }
            if (recordHashSet.contains(toCheck)) {
                result.add(record);
            }
        }
        table.insertAll(result);
        table.mSourceTables = new ArrayList<>();
        table.mSourceTables.addAll(first.getSourceTables());
        return table;
    }

    /**
     * Make two tables has same attributes by join tables which each one doesn't have.
     *
     * @param first first table.
     * @param second second table.
     * @param tables all tables.
     * @return a list of two table as result, in this order: first, second.
     */
    private static ArrayList<Table> preProcessTables(Table first, Table second, Map<String, Table> tables) {
        ArrayList<Table> results = new ArrayList<>();
        results.add(first);
        results.add(second);
        ArrayList<String> firstSources = new ArrayList<>(first.getSourceTables());
        ArrayList<String> secondSources = new ArrayList<>(second.getSourceTables());
        for (String source : secondSources) {
            if (!firstSources.contains(source)) {
                results.set(0, join(results.get(0), tables.get(source), Condition.getAlwaysTrueCondition()));
            }
        }
        for (String source : firstSources) {
            if (!secondSources.contains(source)) {
                results.set(1, join(results.get(1), tables.get(source), Condition.getAlwaysTrueCondition()));
            }
        }
        return results;
    }

    @Override
    public String toString() {
        return ("Table " + mTablename) + "\n";
    }

    /**
     * Append a new DataRecord to the end of the file.
     *
     * @param filename filename to append.
     * @param record record to append.
     * @return true if succeed, false if failed.
     */
    public boolean appendToDisk(String filename, DataRecord record) {
        if (!mDiskModifiable) {
            return true;
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
            writer.write(record.writeToString() + "\n");
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public abstract boolean writeToDisk(String filename);

    @Override
    public abstract boolean restoreFromDisk(String filename);
}

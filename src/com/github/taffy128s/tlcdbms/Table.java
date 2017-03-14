package com.github.taffy128s.tlcdbms;

import java.io.*;
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
        attributeNames.add(Table.AUTO_PRIMARY_KEY_NAME);
        attributeTypes.add(new DataType(DataTypeIdentifier.INT, -1));
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

    @Override
    public String toString() {
        return ("Table " + mTablename) + "\n";
    }

    @Override
    public boolean writeToDisk(String filename) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(mTablename + "\n");
            writer.write(mAttributeNames.size() + "\n");
            for (int i = 0; i < mAttributeNames.size(); ++i) {
                writer.write(mAttributeNames.get(i) + "\0");
                writer.write(mAttributeTypes.get(i).getLimit() + "\n");
            }
            writer.write(mPrimaryKey + "\n");
            ArrayList<DataRecord> dataRecords = getAllRecords();
            writer.write(dataRecords.size() + "\n");
            for (DataRecord record : dataRecords) {
                writer.write(record.writeToString() + "\n");
            }
            writer.close();
            return true;
        } catch (IOException e) {
            System.err.println(filename + ": file I/O error.");
        }
        return false;
    }

    @Override
    public boolean restoreFromDisk(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String input;
            mTablename = reader.readLine();
            int attrSize = Integer.parseInt(reader.readLine());
            for (int i = 0; i < attrSize; ++i) {
                input = reader.readLine();
                String[] attrProperties = input.split("\0");
                mAttributeNames.add(attrProperties[0]);
                int limit = Integer.parseInt(attrProperties[1]);
                if (limit < 0) {
                    mAttributeTypes.add(new DataType(DataTypeIdentifier.INT, -1));
                } else {
                    mAttributeTypes.add(new DataType(DataTypeIdentifier.VARCHAR, limit));
                }
            }
            mPrimaryKey = Integer.parseInt(reader.readLine());
            int recordSize = Integer.parseInt(reader.readLine());
            ArrayList<DataRecord> records = new ArrayList<>();
            while ((input = reader.readLine()) != null) {
                DataRecord record = new DataRecord();
                record.restoreFromString(input);
                records.add(record);
            }
            if (records.size() != recordSize) {
                return false;
            }
            insertAll(records);
            reader.close();
            return true;
        } catch (FileNotFoundException e) {
            System.err.println(filename + ": no such file or directory.");
        } catch (IOException e) {
            System.err.println(filename + ": file I/O error.");
        }
        return false;
    }

    /**
     * Get table type,
     * such as "SETTABLE", "TREETABLE".
     *
     * @return a string of table type.
     */
    public abstract String getTableType();

    /**
     * Insert a data record into table.
     * Assume that data is with valid type.
     * ** NEED TO CALL setAutoPrimaryKey() BEFORE INSERT INTO TABLE. **
     *
     * @param dataRecord data record to be inserted.
     * @return true if succeed, false if failed.
     */
    public abstract boolean insert(DataRecord dataRecord);

    /**
     * Insert all data records into table.
     * Assume that all data are with valid type.
     *
     * @param dataRecords a list of data records.
     * @return true if succeed, false if failed.
     */
    public abstract boolean insertAll(ArrayList<DataRecord> dataRecords);

    /**
     * Get all records in the table.
     *
     * @return an array list of all records.
     */
    public abstract ArrayList<DataRecord> getAllRecords();
}

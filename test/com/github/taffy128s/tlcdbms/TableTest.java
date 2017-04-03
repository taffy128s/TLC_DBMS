package com.github.taffy128s.tlcdbms;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Table JUnit Test
 */
public class TableTest {
    private Table table;
    private Table pTable;
    private ArrayList<DataRecord> dataRecords;

    @Before
    public void setUp() throws Exception {
        ArrayList<String> attributeNames = new ArrayList<>();
        ArrayList<DataType> attributeTypes = new ArrayList<>();
        attributeNames.add("Name");
        attributeNames.add("Gender");
        attributeNames.add("Age");
        attributeNames.add("Depart");
        attributeTypes.add(new DataType(DataTypeIdentifier.VARCHAR, 40));
        attributeTypes.add(new DataType(DataTypeIdentifier.VARCHAR, 40));
        attributeTypes.add(new DataType(DataTypeIdentifier.INT, -1));
        attributeTypes.add(new DataType(DataTypeIdentifier.VARCHAR, 40));
        table = new SetTable("table1", attributeNames, attributeTypes, -1, -1);
        pTable = new SetTable("table2", attributeNames, attributeTypes, 0, -1);
        dataRecords = new ArrayList<>();
        DataRecord record = new DataRecord();
        record.append("ADAS");
        record.append("zxc");
        record.append(50);
        record.append("SS");
        dataRecords.add(record);
        table.insert(record);
        record = new DataRecord();
        record.append("ADAS");
        record.append("asd");
        record.append(20);
        record.append("BS");
        dataRecords.add(record);
        table.insert(record);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void insert() throws Exception {
        assertTrue(table.insert(dataRecords.get(0)));
        assertTrue(pTable.insert(dataRecords.get(0)));
    }

    @Test
    public void setPrimaryKey() throws Exception {
        // nothing
    }

    @Test
    public void getPrimaryKey() throws Exception {
        // nothing
    }

    @Test
    public void getAllRecords() throws Exception {
        // nothing
    }

    @Test
    public void setAttributeNames() throws Exception {
        // nothing
    }

    @Test
    public void getAttributeNames() throws Exception {
        // nothing
    }

    @Test
    public void setAttributeTypes() throws Exception {
        // nothing
    }

    @Test
    public void getAttributeTypes() throws Exception {
        // nothing
    }

    @Test
    public void disk() throws Exception {
        // nothing
    }
}

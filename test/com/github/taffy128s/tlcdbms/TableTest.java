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
        attributeTypes.add(DataType.VARCHAR);
        attributeTypes.add(DataType.VARCHAR);
        attributeTypes.add(DataType.INT);
        attributeTypes.add(DataType.VARCHAR);
        table = new Table(attributeNames, attributeTypes, -1);
        pTable = new Table(attributeNames, attributeTypes, 0);
        dataRecords = new ArrayList<>();
        DataRecord record = new DataRecord();
        record.append("ADAS");
        record.append("zxc");
        record.append(50);
        record.append("SS");
        dataRecords.add(record);
        record = new DataRecord();
        record.append("ADAS");
        record.append("asd");
        record.append(20);
        record.append("BS");
        dataRecords.add(record);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void insert() throws Exception {
        assertEquals(true, table.insert(dataRecords.get(0)));
        assertEquals(true, table.insert(dataRecords.get(1)));
        assertEquals(false, table.insert(dataRecords.get(0)));
        assertEquals(false, table.insert(dataRecords.get(1)));
        assertEquals(true, pTable.insert(dataRecords.get(0)));
        assertEquals(false, pTable.insert(dataRecords.get(1)));
        assertEquals(false, pTable.insert(dataRecords.get(0)));
        assertEquals(false, pTable.insert(dataRecords.get(1)));
    }

    @Test
    public void setPrimaryKey() throws Exception {
        // nothing
    }

    @Test
    public void getPrimaryKey() throws Exception {
        // nothing, expected -1
    }

    @Test
    public void getAllRecords() throws Exception {

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

}

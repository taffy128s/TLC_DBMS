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
        table = new SetTable("FUCK1", attributeNames, attributeTypes, -1, -1);
        pTable = new SetTable("FUCK2", attributeNames, attributeTypes, 0, -1);
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
        assertEquals(false, table.insert(dataRecords.get(0)));
        assertEquals(false, table.insert(dataRecords.get(1)));
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

    @Test
    public void disk() throws Exception {
        System.out.println(System.getProperty("user.dir"));
        String filename = "./out.bin";
        table.writeToDisk(filename);
        SetTable setTable = new SetTable();
        setTable.restoreFromDisk(filename);
        assertEquals(setTable.getTablename(), table.getTablename());
        assertEquals(setTable.getPrimaryKey(), table.getPrimaryKey());
        assertEquals(true, setTable.getAttributeNames().equals(table.getAttributeNames()));
        assertEquals(true, setTable.getAttributeTypes().equals(table.getAttributeTypes()));
        assertEquals(true, setTable.getAllRecords().equals(table.getAllRecords()));
    }
}

package com.github.taffy128s.tlcdbms;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * DataRecord JUnit Test
 */
public class DataRecordTest {
    private DataRecord dataRecord;

    @Before
    public void setUp() throws Exception {
        dataRecord = new DataRecord();
        dataRecord.append("Bird");
        dataRecord.append("Male");
        dataRecord.append(null);
        dataRecord.append(20);
        dataRecord.append("Computer Science");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void append() throws Exception {
        // nothing
    }

    @Test
    public void set() throws Exception {
        dataRecord.set(2, 22);
        assertEquals(22, dataRecord.get(2));
        dataRecord.set(2, 20);
    }

    @Test
    public void get() throws Exception {
        // nothing
    }

    @Test
    public void getAllFields() throws Exception {
        // nothing
    }

    @Test
    public void length() throws Exception {
        assertEquals(5, dataRecord.length());
    }

    @Test
    public void disk() throws Exception {
        // nothing
    }

    @Test
    public void string() throws Exception {
        String str = dataRecord.writeToString();
        DataRecord another = new DataRecord();
        another.restoreFromString(str);
        assertEquals(true, dataRecord.getAllFields().equals(another.getAllFields()));
    }
}

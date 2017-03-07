package com.github.taffy128s.tlcdbms;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * DataChecker JUnit Test
 */
public class DataCheckerTest {
    private ArrayList<String> testSet;

    @Before
    public void setUp() throws Exception {
        testSet = new ArrayList<>();
        testSet.add("111");
        testSet.add("John");
        testSet.add("123456789123");
        testSet.add("2147483647");
        testSet.add("2147483648");
        testSet.add("21474836472123123123123123");
        testSet.add("214748364712323232323232323");
        testSet.add("-2147483648");
        testSet.add("-2147483648111");
        testSet.add("-214748364811111111111111111");
        testSet.add("123sss456");
        testSet.add("123123123123123123123123123123123123123123123123123123");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void isValidInteger() throws Exception {
        ArrayList<Boolean> expected = new ArrayList<>();
        expected.add(true);
        expected.add(false);
        expected.add(false);
        expected.add(true);
        expected.add(false);
        expected.add(false);
        expected.add(false);
        expected.add(true);
        expected.add(false);
        expected.add(false);
        expected.add(false);
        expected.add(false);
        for (int i = 0; i < expected.size(); ++i) {
            assertEquals(expected.get(i), DataChecker.isValidInteger(testSet.get(i)));
        }
    }

    @Test
    public void isValidVarChar() throws Exception {
        ArrayList<Boolean> expected = new ArrayList<>();
        expected.add(true);
        expected.add(true);
        expected.add(true);
        expected.add(true);
        expected.add(true);
        expected.add(true);
        expected.add(true);
        expected.add(true);
        expected.add(true);
        expected.add(true);
        expected.add(true);
        expected.add(false);
        for (int i = 0; i < expected.size(); ++i) {
            assertEquals(expected.get(i), DataChecker.isValidVarChar(testSet.get(i)));
        }
    }

}

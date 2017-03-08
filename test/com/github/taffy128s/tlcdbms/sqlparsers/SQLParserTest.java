package com.github.taffy128s.tlcdbms.sqlparsers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * SQLParser JUnit Test
 */
public class SQLParserTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void parse() throws Exception {
        ArrayList<String> testcases = new ArrayList<>();
        testcases.add("cReate accc(ss int PRiMArY kEy, scv varchar(-10))");
        testcases.add("cReate table accc(ss int PRiMArY kEy, scv varchar(40));");
        testcases.add("cReate table accc(ss int PRiMArY kEy, scv varohar(40))");
        testcases.add("cReate table (ss int, scv varchar(40))");
        testcases.add("cReate table as ss int, scv varchar(40))");
        testcases.add("cREATE table ss(ss INTS, scv VARCHAR(40))");
        testcases.add("Create table ss(ss INT, VARCHAR(40))");
        testcases.add("create table ss(ss INT, scv VARCHAR(-1))");
        testcases.add("create table ss(ss INT PRIMARY KEY, scv INT PRIMARY KEY)");
        testcases.add("Create table f(_ INT PRIMARY KEY, scv INT,)");
        testcases.add("Create table f();");
        SQLParser parser = new SQLParser();
        for (String test : testcases) {
            ParseResult result = parser.parse(test);
        }
    }
}

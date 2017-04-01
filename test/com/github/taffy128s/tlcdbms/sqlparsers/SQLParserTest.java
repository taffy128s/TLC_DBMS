package com.github.taffy128s.tlcdbms.sqlparsers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

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
        testcases.add("create table fuck(a int);");
        testcases.add("create table fuckk(b int);");
        testcases.add("select fuck.*, fuckk.* from fuck, fuckk;");
        SQLParser parser = new SQLParser();
        for (String test : testcases) {
            System.out.println("-----------------------------------");
            System.out.println("'" + test + "'");
            SQLParseResult result = parser.parse(test);
            if (result != null)
                System.out.print(result.toString());
        }
        System.out.println("-----------------------------------");
    }
}

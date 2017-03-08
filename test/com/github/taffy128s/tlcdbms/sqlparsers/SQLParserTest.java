package com.github.taffy128s.tlcdbms.sqlparsers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        SQLParser parser = new SQLParser(
                "cReate table accc(ss int PRiMArY kEy, scv varchar(-10))"
        );
        ParseResult result = parser.parse();
        System.out.println(result);
    }

}

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
        SQLParser parser = new SQLParser();
        parser.parse("cReate accc(ss int PRiMArY kEy, scv varchar(-10))");
        parser.parse("cReate table accc(ss int PRiMArY kEy, scv varchar(40))");
        parser.parse("cReate table accc(ss int PRiMArY kEy, scv varohar(40))");
    }

}

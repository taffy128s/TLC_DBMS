package com.github.taffy128s.tlcdbms;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Condition Unit Test.
 */
public class ConditionTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void calculateCondition() throws Exception {
        Condition condition = new Condition("1", null, null, "2", null, null, BinaryOperator.GREATER_THAN);
        boolean result = Condition.calculateCondition(condition, new DataRecord(), -1, new DataRecord(), -1);
        assertEquals(false, result);
        condition = new Condition("5", null, null, "5", null, null, BinaryOperator.EQUAL);
        result = Condition.calculateCondition(condition, new DataRecord(), -1, new DataRecord(), -1);
        assertEquals(true, result);
    }

    @Test
    public void calculateResult() throws Exception {
        Integer a = 5;
        Integer b = 7;
        boolean result = Condition.calculateResult(a, b, BinaryOperator.LESS_EQUAL);
        assertEquals(true, result);
    }
}

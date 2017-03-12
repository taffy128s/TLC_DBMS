package com.github.taffy128s.btree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * BTree JUnit Test.
 */
public class BTreeTest {
    private BTree<Integer, ArrayList<Integer>> tree;
    private TreeMap<Integer, ArrayList<Integer>> trees;

    @Before
    public void setUp() throws Exception {
        tree = new BTree<>(100);
        trees = new TreeMap<>();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void insert() throws Exception {
        BTree<Integer, Integer> treep = new BTree<>(3);
        treep.put(1, 1);
        treep.put(2, 1);
        treep.put(3, 1);
        treep.put(4, 1);
        treep.put(5, 1);
        System.out.println(treep.getAllValues());
    }

    @Test
    public void find() throws Exception {

    }

    @Test
    public void tableCorrectness() throws Exception {
        ArrayList<Integer> testcase = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 50000; ++i) {
            testcase.add(random.nextInt() % 2000);
        }
        for (int data : testcase) {
            int datain = random.nextInt() % 100;
            if (tree.containsKey(data)) {
                tree.get(data).add(datain);
            } else {
                tree.put(data, new ArrayList<>());
                tree.get(data).add(datain);
            }
            if (trees.containsKey(data)) {
                trees.get(data).add(datain);
            } else {
                trees.put(data, new ArrayList<>());
                trees.get(data).add(datain);
            }
        }
        for (int i = 0; i < 2000; ++i) {
            ArrayList<Integer> result = tree.get(i);
            ArrayList<Integer> correct = trees.get(i);
            if (result == null) {
                result = new ArrayList<>();
            }
            if (correct == null) {
                correct = new ArrayList<>();
            }
            assertEquals(result, correct);
        }
    }
}

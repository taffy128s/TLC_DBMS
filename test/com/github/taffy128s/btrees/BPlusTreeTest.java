package com.github.taffy128s.btrees;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * B Plus Tree JUnit Test.
 */
public class BPlusTreeTest {
    private BPlusTree<Integer, ArrayList<Integer>> tree;
    private TreeMap<Integer, ArrayList<Integer>> trees;

    @Before
    public void setUp() throws Exception {
        tree = new BPlusTree<>(3, 3);
        trees = new TreeMap<>();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void put() throws Exception {

    }

    @Test
    public void get() throws Exception {
        ArrayList<Integer> testcase = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 2000000; ++i) {
            testcase.add(random.nextInt() % 200000);
        }
        for (int test : testcase) {
            int datain = random.nextInt() % 100;
            if (!tree.containsKey(test)) {
                tree.put(test, new ArrayList<>());
            }
            if (!trees.containsKey(test)) {
                trees.put(test, new ArrayList<>());
            }
            tree.get(test).add(datain);
            trees.get(test).add(datain);
        }
        for (int i = 0; i < 100000; ++i) {
            ArrayList<Integer> a = tree.get(i);
            ArrayList<Integer> b = trees.get(i);
            String sa;
            String sb;
            if (a == null) {
                sa = "null";
            } else {
                sa = a.toString();
            }
            if (b == null) {
                sb = "null";
            } else {
                sb = b.toString();
            }
            assertEquals(sa, sb);
        }
    }

    @Test
    public void rangeQuery() throws Exception {
        BPlusTree<Integer, Integer> t = new BPlusTree<>(7, 8);
        System.out.println(t.getValues(5, 99));
        t.put(1, 2);
        t.put(6, 8);
        t.put(3, 6);
        System.out.println(t.getValues(4, 6));
        System.out.println(t.getValues(1, true, 3, true));
        System.out.println(t.getValues(1, false, 3, true));
        System.out.println(t.getValues(1, true, 3, false));
        System.out.println(t.getValues(1, false, 3, false));
        BPlusTree<Integer, Integer> tt = new BPlusTree<>(7, 8);
        TreeMap<Integer, Integer> ts = new TreeMap<>();
        ArrayList<Integer> testcase = new ArrayList<>();
        ArrayList<Integer> dataIn = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 1000000; ++i) {
            testcase.add(i);
            dataIn.add(i);
        }
        Collections.shuffle(testcase);
        Collections.shuffle(dataIn);
        for (int i = 0; i < 300000; ++i) {
            tt.put(testcase.get(i), dataIn.get(i));
            ts.put(testcase.get(i), dataIn.get(i));
        }
        assertEquals(tt.size(), ts.size());
        assertEquals(tt.getKeys().size(), ts.size());
        assertEquals(tt.getValues().size(), ts.size());
        for (int i = 0; i < 150; ++i) {
            int from = random.nextInt() % 300000;
            int to = random.nextInt() % 300000;
            int low = Math.min(from, to);
            int high = Math.max(from, to);

            ArrayList<Integer> ansK = tt.getKeys(low, high);
            ArrayList<Integer> ansV = tt.getValues(low, high);
            Map<Integer, Integer> tss = ts.subMap(low, high);
            assertEquals(ansV.size(), tss.size());
            assertEquals(ansK.size(), tss.size());
            for (int j = 0; j < ansK.size(); ++j) {
                assertEquals(true, tss.containsKey(ansK.get(j)));
                assertEquals(0, tss.get(ansK.get(j)).compareTo(ansV.get(j)));
            }

            ansK = tt.getKeysLess(high);
            ansV = tt.getValuesLess(high);
            tss = ts.subMap(-10000000, high);
            assertEquals(ansK.size(), tss.size());
            assertEquals(ansV.size(), tss.size());
            for (int j = 0; j < ansK.size(); ++j) {
                assertEquals(true, tss.containsKey(ansK.get(j)));
                assertEquals(0, tss.get(ansK.get(j)).compareTo(ansV.get(j)));
            }

            ansK = tt.getKeysLessEqual(high);
            ansV = tt.getValuesLessEqual(high);
            tss = ts.subMap(-10000000, high + 1);
            assertEquals(ansK.size(), tss.size());
            assertEquals(ansV.size(), tss.size());
            for (int j = 0; j < ansK.size(); ++j) {
                assertEquals(true, tss.containsKey(ansK.get(j)));
                assertEquals(0, tss.get(ansK.get(j)).compareTo(ansV.get(j)));
            }

            ansK = tt.getKeysGreater(low);
            ansV = tt.getValuesGreater(low);
            tss = ts.subMap(low + 1, 10000000);
            assertEquals(ansK.size(), tss.size());
            assertEquals(ansV.size(), tss.size());
            for (int j = 0; j < ansK.size(); ++j) {
                assertEquals(true, tss.containsKey(ansK.get(j)));
                assertEquals(0, tss.get(ansK.get(j)).compareTo(ansV.get(j)));
            }

            ansK = tt.getKeysGreaterEqual(low);
            ansV = tt.getValuesGreaterEqual(low);
            tss = ts.subMap(low, 10000000);
            assertEquals(ansK.size(), tss.size());
            assertEquals(ansV.size(), tss.size());
            for (int j = 0; j < ansK.size(); ++j) {
                assertEquals(true, tss.containsKey(ansK.get(j)));
                assertEquals(0, tss.get(ansK.get(j)).compareTo(ansV.get(j)));
            }
        }
    }

    @Test
    public void sequentialVisit() throws Exception {

    }
}

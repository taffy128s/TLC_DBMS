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
        BPlusTree<Integer, Integer> bt = new BPlusTree<>(100, 100);
        bt.put(5, 7);
        bt.put(5, 9);
        System.out.println(bt.getKeys().size());
    }

    @Test
    public void get() throws Exception {
        ArrayList<Integer> testcase = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 2000000; ++i) {
            testcase.add(random.nextInt() % 200000);
        }
        for (int test : testcase) {
            int datain = random.nextInt() % 10000;
            if (!tree.containsKey(test)) {
                tree.put(test, new ArrayList<>());
            }
            if (!trees.containsKey(test)) {
                trees.put(test, new ArrayList<>());
            }
            tree.get(test).add(datain);
            trees.get(test).add(datain);
        }
        for (int i = 0; i < 500000; ++i) {
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
        BPlusTree<Integer, Integer> t = new BPlusTree<>(100, 1000);
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
        for (int i = 0; i < 700000; ++i) {
            tt.put(testcase.get(i), dataIn.get(i));
            ts.put(testcase.get(i), dataIn.get(i));
        }
        for (int i = 5500; i < 675000; ++i) {
            tt.put(testcase.get(i), dataIn.get(i - 7));
            ts.put(testcase.get(i), dataIn.get(i - 7));
        }
        assertEquals(tt.size(), ts.size());
        assertEquals(tt.getKeys().size(), ts.size());
        assertEquals(tt.getValues().size(), ts.size());
        ArrayList<Integer> allKeys = tt.getKeys();
        ArrayList<Integer> allValues = tt.getValues();
        allValues.sort(Comparator.naturalOrder());
        for (int key : ts.keySet()) {
            assertEquals(ts.get(key), tt.get(key));
            assertEquals(true, Collections.binarySearch(allKeys, key) >= 0);
            assertEquals(true, Collections.binarySearch(allValues, tt.get(key)) >= 0);
        }
        for (int i = 0; i < 200; ++i) {
            int from = random.nextInt() % 300000;
            int to = random.nextInt() % 300000;
            if (from < 0) {
                from *= (-1);
            }
            if (to < 0) {
                to *= (-1);
            }
            int low = Math.min(from, to);
            int high = Math.max(from, to);

            ArrayList<Integer> ansK = tt.getKeys(low, high);
            ArrayList<Integer> ansV = tt.getValues(low, high);
            Map<Integer, Integer> tss = ts.subMap(low, high);
            assertEquals(ansV.size(), tss.size());
            assertEquals(ansK.size(), tss.size());
            for (int j = 0; j < ansK.size(); ++j) {
                assertEquals(true, tss.containsKey(ansK.get(j)));
                assertEquals(true, tss.get(ansK.get(j)).equals(ansV.get(j)));
            }

            ansK = tt.getKeysLess(high);
            ansV = tt.getValuesLess(high);
            tss = ts.subMap(-10000000, high);
            assertEquals(ansK.size(), tss.size());
            assertEquals(ansV.size(), tss.size());
            for (int j = 0; j < ansK.size(); ++j) {
                assertEquals(true, tss.containsKey(ansK.get(j)));
                assertEquals(true, tss.get(ansK.get(j)).equals(ansV.get(j)));
            }

            ansK = tt.getKeysLessEqual(high);
            ansV = tt.getValuesLessEqual(high);
            tss = ts.subMap(-10000000, high + 1);
            assertEquals(ansK.size(), tss.size());
            assertEquals(ansV.size(), tss.size());
            for (int j = 0; j < ansK.size(); ++j) {
                assertEquals(true, tss.containsKey(ansK.get(j)));
                assertEquals(true, tss.get(ansK.get(j)).equals(ansV.get(j)));
            }

            ansK = tt.getKeysGreater(low);
            ansV = tt.getValuesGreater(low);
            tss = ts.subMap(low + 1, 10000000);
            assertEquals(ansK.size(), tss.size());
            assertEquals(ansV.size(), tss.size());
            for (int j = 0; j < ansK.size(); ++j) {
                assertEquals(true, tss.containsKey(ansK.get(j)));
                assertEquals(true, tss.get(ansK.get(j)).equals(ansV.get(j)));
            }

            ansK = tt.getKeysGreaterEqual(low);
            ansV = tt.getValuesGreaterEqual(low);
            tss = ts.subMap(low, 10000000);
            assertEquals(ansK.size(), tss.size());
            assertEquals(ansV.size(), tss.size());
            for (int j = 0; j < ansK.size(); ++j) {
                assertEquals(true, tss.containsKey(ansK.get(j)));
                assertEquals(true, tss.get(ansK.get(j)).equals(ansV.get(j)));
            }
        }
    }

    @Test
    public void sequentialVisit() throws Exception {

    }

    @Test
    public void duplicateKeys() throws Exception {
        ArrayList<Integer> keys = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 1000000; ++i) {
            keys.add(random.nextInt() % 20000);
            values.add(i);
        }
        BPlusTree<Integer, Integer> bt = new BPlusTree<>(10, 10);
        TreeMap<Integer, Integer> tm = new TreeMap<>();
        for (int i = 0; i < 1000000; ++i) {
            bt.put(keys.get(i), values.get(i));
            tm.put(keys.get(i), values.get(i));
        }
        System.out.println(tm.size());
        System.out.println(bt.getKeys().size());
        assertEquals(tm.size(), bt.size());
        assertEquals(tm.size(), bt.getKeys().size());
        for (int key : tm.keySet()) {
            assertEquals(tm.get(key), bt.get(key));
        }
    }

    @Test
    public void keysTest() throws Exception {
        ArrayList<Integer> testKeys = new ArrayList<>();
        ArrayList<Integer> testValues = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 1000000; ++i) {
            testKeys.add(random.nextInt() % 100000);
            testValues.add(random.nextInt() % 100000);
        }
        BPlusTree<Integer, Integer> bt = new BPlusTree<>(10, 10);
        TreeMap<Integer, Integer> tm = new TreeMap<>();
        for (int i = 0; i < 1000000; ++i) {
            bt.put(testKeys.get(i), testValues.get(i));
            tm.put(testKeys.get(i), testValues.get(i));
        }
        ArrayList<Integer> keyAns = bt.getKeys(5000, 100000);
        System.out.println(keyAns.size());
        HashSet<Integer> set = new HashSet<>();
        for (int k : keyAns) {
            if (set.contains(k)) {
                System.out.println("DUPLICATED: " + k);
            }
            set.add(k);
            if (!tm.subMap(5000, 100000).containsKey(k)) {
                System.out.println("no: " + k);
            }
        }
        System.out.println(bt.getKeys().size());
        System.out.println(bt.getValues().size());
        System.out.println(tm.size());
    }

    @Test
    public void construct() throws Exception {
        ArrayList<Integer> testKeys = new ArrayList<>();
        ArrayList<Integer> testValues = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 1000000; ++i) {
            testKeys.add(i);
            testValues.add(random.nextInt() % 100000);
        }
        BPlusTree<Integer, Integer> bt = new BPlusTree<>(10, 10);
        TreeMap<Integer, Integer> tm = new TreeMap<>();
        bt.construct(testKeys, testValues);
        for (int i = 0; i < 1000000; ++i) {
            tm.put(testKeys.get(i), testValues.get(i));
        }
        ArrayList<Integer> keyAns = bt.getKeys(5000, 100000);
        System.out.println(keyAns.size());
        HashSet<Integer> set = new HashSet<>();
        for (int k : keyAns) {
            if (set.contains(k)) {
                System.out.println("DUPLICATED: " + k);
            }
            set.add(k);
            if (!tm.subMap(5000, 100000).containsKey(k)) {
                System.out.println("no: " + k);
            }
        }
        System.out.println(bt.size());
        System.out.println(bt.getKeys().size());
        System.out.println(bt.getValues().size());
        System.out.println(tm.size());
    }

    @Test
    public void constructSpeed() throws Exception {
        for (int t = 0; t < 20; ++t) {
            ArrayList<Integer> testKeys = new ArrayList<>();
            ArrayList<Integer> testValues = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < 50000; ++i) {
                testKeys.add(i);
                testValues.add(random.nextInt() % 100000);
            }
            BPlusTree<Integer, Integer> bt = new BPlusTree<>(10, 1000);
            bt.construct(testKeys, testValues);
            System.out.println(bt.size());
        }
    }
}

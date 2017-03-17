package com.github.taffy128s.btrees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * B Tree Implementation.
 *
 * Currently only support INSERT, FIND (no deletion),
 * since deletion is not needed in whole project.
 *
 * @param <K> key type.
 * @param <V> value type.
 */
public class BTree<K, V> {
    /**
     * Tree Node of B Tree.
     */
    private class BTreeNode {
        private ArrayList<K> mKeys;
        private ArrayList<V> mValues;
        private ArrayList<BTreeNode> mNext;

        /**
         * Initialize a tree node.
         */
        public BTreeNode() {
            mKeys = new ArrayList<>();
            mValues = new ArrayList<>();
            mNext = new ArrayList<>();
            mNext.add(null);
        }

        /**
         * Put a pair ((key, value), nextTreeNode) into this tree node.
         *
         * @param key key data.
         * @param value value data.
         * @param next next object, null if not exists.
         * @return 1 if new data added, 0 if only modify existed one.
         */
        public int put(K key, V value, BTreeNode next) {
            int index = Collections.binarySearch(mKeys, key, mComparator);
            if (index >= 0) {
                mValues.set(index, value);
                return 0;
            } else {
                mKeys.add((-1) * index - 1, key);
                mValues.add((-1) * index - 1, value);
                mNext.add((-1) * index, next);
                return 1;
            }
        }

        /**
         * Get value with corresponding key in this node.
         *
         * @param key key to get.
         * @return value with corresponding key.
         */
        public V get(K key) {
            int index = Collections.binarySearch(mKeys, key, mComparator);
            if (index >= 0) {
                return mValues.get(index);
            } else {
                return null;
            }
        }

        /**
         * Split the node into two part. This node becomes p, the node returned
         * by function becomes q. Note that p got all elements < medium value, q
         * got all elements > medium value. This function will change the data
         * size stored inside, so remember to store the medium value to another
         * place.
         *
         * @return q tree node.
         */
        public BTreeNode split() {
            BTreeNode rightNode = splitRight();
            int midIndex = mKeys.size() / 2;
            mKeys.subList(midIndex, mKeys.size()).clear();
            mValues.subList(midIndex, mValues.size()).clear();
            mNext.subList(midIndex + 1, mNext.size()).clear();
            return rightNode;
        }

        /**
         * Get medium value of key of this node.
         * (With index size / 2).
         *
         * @return medium key.
         */
        public K getMidKey() {
            return mKeys.get(mKeys.size() / 2);
        }

        /**
         * Get medium value of value of this node.
         * (With index size / 2).
         *
         * @return medium value.
         */
        public V getMidValue() {
            return mValues.get(mValues.size() / 2);
        }

        /**
         * Setter of key.
         *
         * @param index index to set.
         * @param key key to set.
         */
        public void setKey(int index, K key) {
            mKeys.set(index, key);
        }

        /**
         * Setter of keys.
         *
         * @param keys a list of keys to set.
         */
        public void setKeys(List<K> keys) {
            mKeys = new ArrayList<>();
            mKeys.addAll(keys);
        }

        /**
         * Getter of key.
         *
         * @param index index to get.
         * @return key with corresponding index.
         */
        public K getKey(int index) {
            return mKeys.get(index);
        }

        /**
         * Getter of key.
         *
         * @return a list of all keys in this node.
         */
        public ArrayList<K> getKeys() {
            return mKeys;
        }

        /**
         * Value setter.
         *
         * @param index index to set.
         * @param value value to set.
         */
        public void setValue(int index, V value) {
            mValues.set(index, value);
        }

        /**
         * Values setter.
         *
         * @param values a list of values to set.
         */
        public void setValues(List<V> values) {
            mValues = new ArrayList<>();
            mValues.addAll(values);
        }

        /**
         * Value getter.
         *
         * @param index index to get.
         * @return value with corresponding index.
         */
        public V getValue(int index) {
            return mValues.get(index);
        }

        /**
         * Value getter.
         *
         * @return a list of all values in this node.
         */
        public ArrayList<V> getValues() {
            return mValues;
        }

        /**
         * Next setter.
         *
         * @param index index to set.
         * @param next value to set.
         */
        public void setNext(int index, BTreeNode next) {
            mNext.set(index, next);
        }

        /**
         * Next setter.
         *
         * @param values a list of value to set.
         */
        public void setNext(List<BTreeNode> values) {
            mNext = new ArrayList<>();
            mNext.addAll(values);
        }

        /**
         * Next getter.
         *
         * @param index index to get.
         * @return Next TreeNode with corresponding index in next array list.
         */
        public BTreeNode getNext(int index) {
            if (index >= 0 && index < mNext.size()) {
                return mNext.get(index);
            } else {
                return null;
            }
        }

        /**
         * Next getter.
         *
         * @return a list of next of this node.
         */
        public ArrayList<BTreeNode> getNexts() {
            return mNext;
        }

        /**
         * Number of data stored in this node.
         *
         * @return data size in this node.
         */
        public int size() {
            return mKeys.size();
        }

        /**
         * Check whether this node is empty.
         *
         * @return true if there is no key in this node, false otherwise.
         */
        public boolean isEmpty() {
            return mKeys.isEmpty();
        }

        /**
         * Check whether this node is full.
         * That is, size >= order.
         *
         * @return true if full, false otherwise.
         */
        public boolean isFull() {
            return mKeys.size() >= mOrder;
        }

        /**
         * Get the q part when split.
         *
         * @return a TreeNode with datas belongs to q.
         */
        private BTreeNode splitRight() {
            int midIndex = mKeys.size() / 2;
            BTreeNode result = new BTreeNode();
            result.setKeys(mKeys.subList(midIndex + 1, mKeys.size()));
            result.setValues(mValues.subList(midIndex + 1, mValues.size()));
            result.setNext(mNext.subList(midIndex + 1, mNext.size()));
            return result;
        }

        @Override
        public String toString() {
            return "BTreeNode{Keys " + mKeys + "}";
        }
    }

    /**
     * BTree Find Result.
     * A type returned by get().
     */
    private class BTreeFindResult {
        public BTreeNode mResult;
        public int mIndex;

        /**
         * Initialize.
         *
         * @param result result tree node.
         * @param index index of the key value in result tree node.
         */
        public BTreeFindResult(BTreeNode result, int index) {
            mResult = result;
            mIndex = index;
        }

        /**
         * Result getter.
         *
         * @return result tree node.
         */
        public BTreeNode getResult() {
            return mResult;
        }

        /**
         * Index getter.
         *
         * @return index.
         */
        public int getIndex() {
            return mIndex;
        }
    }

    private int mOrder;
    private int mSize;
    private BTreeNode mRoot;

    private Comparator<? super K> mComparator;

    /**
     * Initialize a b tree with order given.
     *
     * @param order order of this tree.
     *              Need >= 3.
     */
    public BTree(int order) {
        if (order < 3) {
            order = 3;
        }
        mOrder = order;
        mSize = 0;
        mRoot = new BTreeNode();
        mComparator = (o1, o2) -> ((Comparable) o1).compareTo(o2);
    }

    /**
     * Initialize a b tree with order given.
     *
     * @param order order of this tree.
     *              Need >= 3.
     * @param comparator comparator.
     */
    public BTree(int order, Comparator<? super K> comparator) {
        if (order < 3) {
            order = 3;
        }
        mOrder = order;
        mSize = 0;
        mRoot = new BTreeNode();
        mComparator = comparator;
    }

    /**
     * Insert a data pair (key, value) into tree.
     *
     * @param key key data to insert.
     * @param value value data to insert.
     */
    public void put(K key, V value) {
        insertTreeNode(mRoot, null, key, value);
    }

    /**
     * Get value with corresponding key value given.
     *
     * @param key key data to search.
     * @return corresponding value, null if not represent.
     */
    public V get(K key) {
        BTreeFindResult result = findTreeNode(mRoot, null, key);
        BTreeNode p = result.getResult();
        int index = result.getIndex();
        if (index >= 0) {
            return p.getValue(index);
        } else {
            return null;
        }
    }

    /**
     * Get all keys in this tree.
     *
     * @return a list of keys in this tree.
     */
    public ArrayList<K> getKeys() {
        ArrayList<K> allKeys = new ArrayList<>();
        getKeys(mRoot, allKeys);
        return allKeys;
    }

    /**
     * Get all values in this tree.
     *
     * @return a list of values in this tree.
     */
    public ArrayList<V> getValues() {
        ArrayList<V> allValues = new ArrayList<>();
        getValues(mRoot, allValues);
        return allValues;
    }

    /**
     * Check whether the key is in this tree.
     *
     * @param key key to check.
     * @return true if it is in this tree, false otherwise.
     */
    public boolean containsKey(K key) {
        BTreeFindResult result = findTreeNode(mRoot, null, key);
        return result.getIndex() != -1;
    }

    /**
     * Check whether this tree is empty.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return mRoot.isEmpty();
    }

    /**
     * Get size of this tree.
     *
     * @return size of this tree.
     */
    public int size() {
        return mSize;
    }

    /**
     * Find Tree Node with key value given.
     *
     * @param root root tree node(this.mRoot if first call).
     * @param parent parent tree node(null if first call).
     * @param key key to search.
     * @return a find result.
     */
    private BTreeFindResult findTreeNode(BTreeNode root, BTreeNode parent, K key) {
        if (root == null) {
            return new BTreeFindResult(parent, -1);
        }
        if (root.isEmpty()) {
            return new BTreeFindResult(root, -1);
        }
        int index = Collections.binarySearch(root.getKeys(), key, mComparator);
        if (index >= 0) {
            return new BTreeFindResult(root, index);
        } else {
            return findTreeNode(root.getNext((-1) * index - 1), root, key);
        }
    }

    /**
     * Insert key, value into this tree.
     *
     * @param root root tree node(this.mRoot if first call).
     * @param parent parent tree node(null if first call).
     * @param key key to insert.
     * @param value value to insert.
     */
    private void insertTreeNode(BTreeNode root, BTreeNode parent, K key, V value) {
        if (root == null) {
            mSize += parent.put(key, value, null);
            return;
        }
        int index = Collections.binarySearch(root.getKeys(), key, mComparator);
        if (index >= 0) {
            root.setValue(index, value);
        } else {
            insertTreeNode(root.getNext((-1) * index - 1), root, key, value);
            if (root.isFull()) {
                K keyElement = root.getMidKey();
                V valueElement = root.getMidValue();
                BTreeNode q = root.split();
                if (parent == null) {
                    parent = new BTreeNode();
                    parent.setNext(0, root);
                    parent.put(keyElement, valueElement, q);
                    mRoot = parent;
                } else {
                    parent.put(keyElement, valueElement, q);
                }
            }
        }
    }

    /**
     * Get all keys of this tree.
     *
     * @param root root tree node(mRoot if first call).
     * @param result a list to store results.
     */
    private void getKeys(BTreeNode root, ArrayList<K> result) {
        if (root == null) {
            return;
        }
        result.addAll(root.getKeys());
        for (BTreeNode next : root.getNexts()) {
            getKeys(next, result);
        }
    }

    /**
     * Get all values of this tree.
     *
     * @param root root tree node(mRoot if first call).
     * @param result a list to store results.
     */
    private void getValues(BTreeNode root, ArrayList<V> result) {
        if (root == null) {
            return;
        }
        result.addAll(root.getValues());
        for (BTreeNode next : root.getNexts()) {
            getValues(next, result);
        }
    }
}

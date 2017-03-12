package com.github.taffy128s.btrees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * B Tree Implementation.
 *
 * Currently only support INSERT, FIND (no deletion),
 * since deletion is not needed in whole project.
 */
public class BTree<K extends Comparable<K>, V> {
    /**
     * Tree Node of B Tree.
     */
    private class BTreeNode {
        private int mOrder;
        private ArrayList<K> mKeys;
        private ArrayList<V> mValues;
        private ArrayList<BTreeNode> mNext;

        /**
         * Initialize a tree node.
         *
         * @param order order of this node(equals to tree).
         *              Need to greater than or equal to 3.
         */
        public BTreeNode(int order) {
            if (order < 3) {
                System.err.println("Order of a b-tree need to be >= 3");
                assert order >= 3;
            }
            mOrder = order;
            mKeys = new ArrayList<>();
            mValues = new ArrayList<>();
            mNext = new ArrayList<>();
            mNext.add(null);
        }

        /**
         * Add a pair ((key, value), nextTreeNode) into this tree node.
         *
         * @param key key data.
         * @param value value data.
         * @param next next object(null-able).
         */
        public void add(K key, V value, BTreeNode next) {
            int index = Collections.binarySearch(mKeys, key);
            if (index >= 0) {
                mValues.set(index, value);
            } else {
                mKeys.add((-1) * index - 1, key);
                mValues.add((-1) * index - 1, value);
                mNext.add((-1) * index, next);
            }
        }

        /**
         * Split the node into two part. This node is p, the node returned
         * by function is q. Note that p got all elements < medium value, q
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
         * Setter of both key and value.
         *
         * @param keys a list of keys to set.
         * @param values a list of values to set.
         */
        public void setKeyAndValue(List<K> keys, List<V> values) {
            mKeys = new ArrayList<>();
            mValues = new ArrayList<>();
            mKeys.addAll(keys);
            mValues.addAll(values);
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
            mNext.clear();
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
            BTreeNode result = new BTreeNode(mOrder);
            result.setKeyAndValue(mKeys.subList(midIndex + 1, mKeys.size()), mValues.subList(midIndex + 1, mValues.size()));
            result.setNext(mNext.subList(midIndex + 1, mNext.size()));
            return result;
        }

        /**
         * Override Object.toString().
         *
         * @return a string representation of the object.
         */
        @Override
        public String toString() {
            return "{Keys " + mKeys + "}";
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
    private BTreeNode mRoot;

    /**
     * Initialize a b tree with order given.
     *
     * @param order order of this tree.
     *              Need >= 3.
     */
    public BTree(int order) {
        if (order < 3) {
            System.err.println("Order of a b-tree need to be >= 3");
            assert order >= 3;
        }
        mOrder = order;
        mRoot = new BTreeNode(mOrder);
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
     * Get all values in this tree.
     *
     * @return a list of values in this tree.
     */
    public ArrayList<V> getAllValues() {
        ArrayList<V> allValues = new ArrayList<>();
        getAllValues(mRoot, allValues);
        return allValues;
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
        int index = Collections.binarySearch(root.getKeys(), key);
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
            parent.add(key, value, null);
            return;
        }
        int index = Collections.binarySearch(root.getKeys(), key);
        if (index >= 0) {
            root.setValue(index, value);
        } else {
            insertTreeNode(root.getNext((-1) * index - 1), root, key, value);
            if (root.isFull()) {
                K keyElement = root.getMidKey();
                V valueElement = root.getMidValue();
                BTreeNode q = root.split();
                if (parent == null) {
                    parent = new BTreeNode(mOrder);
                    parent.setNext(0, root);
                    parent.add(keyElement, valueElement, q);
                    mRoot = parent;
                } else {
                    parent.add(keyElement, valueElement, q);
                }
            }
        }
    }

    /**
     * Get all values of this tree.
     *
     * @param root root tree node(mRoot if first call).
     * @param result a list to store results.
     */
    private void getAllValues(BTreeNode root, ArrayList<V> result) {
        if (root == null) {
            return;
        }
        result.addAll(root.getValues());
        for (BTreeNode next : root.getNexts()) {
            getAllValues(next, result);
        }
    }
}

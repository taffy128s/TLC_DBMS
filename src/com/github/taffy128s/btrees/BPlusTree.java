package com.github.taffy128s.btrees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * B Plus Tree implementation.
 *
 * Currently only support INSERT, FIND, RANGE FIND (no deletion),
 * since deletion is not needed in whole project.
 *
 * @param <K> key type.
 * @param <V> value type.
 */
public class BPlusTree<K, V> {
    /**
     * B Plus Tree data block.
     */
    private class BPlusTreeData {
        private ArrayList<K> mKeys;
        private ArrayList<V> mValues;
        private BPlusTreeData mNext;
        private BPlusTreeData mPrev;

        /**
         * Initialize a new Data Block.
         */
        BPlusTreeData() {
            mKeys = new ArrayList<>();
            mValues = new ArrayList<>();
            mNext = null;
            mPrev = null;
        }

        /**
         * Add a data pair (key, value) into this data block.
         *
         * @param key key to insert.
         * @param value value to insert.
         * @return 1 if a new data added, 0 if only modified existed one.
         */
        public int put(K key, V value) {
            int index = Collections.binarySearch(mKeys, key, mComparator);
            if (index >= 0) {
                mValues.set(index, value);
                return 0;
            } else {
                mKeys.add((-1 * index - 1), key);
                mValues.add((-1 * index - 1), value);
                return 1;
            }
        }

        /**
         * Append a data pair(key, value) directly into the end of this data block.
         *
         * @param key key to insert.
         * @param value value to insert.
         * @return always 1 (always add a new data).
         */
        public int putLast(K key, V value) {
            mKeys.add(key);
            mValues.add(value);
            return 1;
        }

        /**
         * Get value with corresponding key.
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
         * Split this data block to two part. This becomes the one contains all
         * data less than medium key. The part returned by this function contains
         * all data greater than or equals to medium key. This function will change
         * data stored in this data block, so remember to store medium value to
         * another place.
         *
         * @return the part with all data greater than or equal to medium key.
         */
        public BPlusTreeData split() {
            BPlusTreeData rightNode = splitRight();
            int midIndex = mKeys.size() / 2;
            mKeys.subList(midIndex, mKeys.size()).clear();
            mValues.subList(midIndex, mValues.size()).clear();
            BPlusTreeData originNext = mNext;
            mNext = rightNode;
            rightNode.mNext = originNext;
            rightNode.mPrev = this;
            return rightNode;
        }

        /**
         * Get medium key.
         * (i.e. index with size() / 2).
         *
         * @return medium key.
         */
        public K getMidKey() {
            return mKeys.get(mKeys.size() / 2);
        }

        /**
         * Get medium value.
         * (i.e. index with size() / 2).
         *
         * @return medium value.
         */
        public V getMidValue() {
            return mValues.get(mValues.size() / 2);
        }

        /**
         * Set next data block.
         * (Part to implement a linked list).
         *
         * @param next next data block.
         */
        public void setNext(BPlusTreeData next) {
            mNext = next;
        }

        /**
         * Get next data block.
         * (Part to implement a linked list).
         *
         * @return next data block.
         */
        public BPlusTreeData getNext() {
            return mNext;
        }

        /**
         * Set previous data block.
         * (Part to implement a linked list).
         *
         * @param prev previous data block.
         */
        public void setPrev(BPlusTreeData prev) {
            mPrev = prev;
        }

        /**
         * Get previous data block.
         *
         * @return previous data block.
         */
        public BPlusTreeData getPrev() {
            return mPrev;
        }

        /**
         * Get key with corresponding index.
         *
         * @param index index to get.
         * @return key with corresponding index.
         */
        public K getKey(int index) {
            return mKeys.get(index);
        }

        /**
         * Get all keys.
         *
         * @return a list of keys of this data block.
         */
        public ArrayList<K> getKeys() {
            return mKeys;
        }

        /**
         * Set key with corresponding index.
         *
         * @param index index to set.
         * @param key key to set.
         */
        public void setKey(int index, K key) {
            mKeys.set(index, key);
        }

        /**
         * Set all keys.
         *
         * @param keys a list of keys to set.
         */
        public void setKeys(List<K> keys) {
            mKeys = new ArrayList<>();
            mKeys.addAll(keys);
        }

        /**
         * Get value with corresponding index.
         *
         * @param index index to get.
         * @return value with corresponding index.
         */
        public V getValue(int index) {
            return mValues.get(index);
        }

        /**
         * Get all values of this data block.
         *
         * @return a list of all values of this data block.
         */
        public ArrayList<V> getValues() {
            return mValues;
        }

        /**
         * Set value with corresponding index.
         *
         * @param index index to set.
         * @param value value to set.
         */
        public void setValue(int index, V value) {
            mValues.set(index, value);
        }

        /**
         * Set all values.
         *
         * @param values a list of values to set.
         */
        public void setValues(List<V> values) {
            mValues = new ArrayList<>();
            mValues.addAll(values);
        }

        /**
         * Get size of this data block.
         * @return size of this data block.
         */
        public int size() {
            return mKeys.size();
        }

        /**
         * Check whether this data block is full.
         * (i.e. size >= capacity).
         *
         * @return true if full, false otherwise.
         */
        public boolean isFull() {
            return mKeys.size() >= mCapacity;
        }

        @Override
        public String toString() {
            return mKeys.toString();
        }

        /**
         * Get right part of this data block.
         * (Contains all data greater than or equal to medium value).
         *
         * @return right part of this data block.
         */
        private BPlusTreeData splitRight() {
            int midIndex = mKeys.size() / 2;
            BPlusTreeData rightNode = new BPlusTreeData();
            rightNode.setKeys(mKeys.subList(midIndex, mKeys.size()));
            rightNode.setValues(mValues.subList(midIndex, mValues.size()));
            return rightNode;
        }
    }

    /**
     * B Plus Tree Node.
     */
    private class BPlusTreeNode {
        private ArrayList<K> mKeys;
        private ArrayList<BPlusTreeData> mValues;
        private ArrayList<BPlusTreeNode> mNext;

        /**
         * Initialize a tree node.
         */
        public BPlusTreeNode() {
            mKeys = new ArrayList<>();
            mValues = new ArrayList<>();
            mNext = new ArrayList<>();
            mValues.add(null);
            mNext.add(null);
        }

        /**
         * Put a key value pair into tree node data block.
         *
         * @param key key to insert.
         * @param value value to insert.
         * @param next next TreeNode, null if none.
         * @return 1 if a new data added, 0 if only modify existed one.
         */
        public int put(K key, BPlusTreeData value, BPlusTreeNode next) {
            int index = Collections.binarySearch(mKeys, key, mComparator);
            if (index >= 0) {
                mValues.set(index + 1, value);
                return 0;
            } else {
                mKeys.add((-1) * index - 1, key);
                mValues.add((-1) * index, value);
                mNext.add((-1) * index, next);
                return 1;
            }
        }

        /**
         * Get data block with corresponding key.
         *
         * @param key key to get.
         * @return data block with corresponding key.
         */
        public BPlusTreeData get(K key) {
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
        public BPlusTreeNode split() {
            BPlusTreeNode rightNode = splitRight();
            int midIndex = mKeys.size() / 2;
            mKeys.subList(midIndex, mKeys.size()).clear();
            mValues.subList(midIndex + 1, mValues.size()).clear();
            mNext.subList(midIndex + 1, mNext.size()).clear();
            return rightNode;
        }

        /**
         * Get medium key.
         *
         * @return medium key.
         */
        public K getMidKey() {
            return mKeys.get(mKeys.size() / 2);
        }

        /**
         * Get medium value.
         *
         * @return medium value.
         */
        public BPlusTreeData getMidValue() {
            return mValues.get(mValues.size() / 2);
        }

        /**
         * Get key with corresponding index.
         *
         * @param index index to get.
         * @return key with corresponding index.
         */
        public K getKey(int index) {
            return mKeys.get(index);
        }

        /**
         * Get all keys in this tree node.
         *
         * @return a list of all keys in this tree node.
         */
        public ArrayList<K> getKeys() {
            return mKeys;
        }

        /**
         * Set key with corresponding index.
         *
         * @param index index to set.
         * @param key key to set.
         */
        public void setKey(int index, K key) {
            mKeys.set(index, key);
        }

        /**
         * Set all keys in this tree node.
         *
         * @param keys a list of keys to set.
         */
        public void setKeys(List<K> keys) {
            mKeys = new ArrayList<>();
            mKeys.addAll(keys);
        }

        /**
         * Get value with corresponding index.
         *
         * @param index index to get.
         * @return tree data block with corresponding index.
         */
        public BPlusTreeData getValue(int index) {
            if (index >= 0 && index < mValues.size()) {
                return mValues.get(index);
            } else {
                return null;
            }
        }

        /**
         * Get all values in this tree node.
         *
         * @return a list of values in this tree node.
         */
        public ArrayList<BPlusTreeData> getValues() {
            return mValues;
        }

        /**
         * Set value with corresponding index.
         *
         * @param index index to set.
         * @param treeData tree data to set.
         */
        public void setValue(int index, BPlusTreeData treeData) {
            mValues.set(index, treeData);
        }

        /**
         * Set all values in this tree node.
         *
         * @param values a list of values to set.
         */
        public void setValues(List<BPlusTreeData> values) {
            mValues = new ArrayList<>();
            mValues.addAll(values);
        }

        /**
         * Set next tree node with corresponding index.
         *
         * @param index index to set.
         * @param next next tree node to set.
         */
        public void setNext(int index, BPlusTreeNode next) {
            mNext.set(index, next);
        }

        /**
         * Set all next tree nodes in this tree node.
         *
         * @param values a list of tree nodes to set.
         */
        public void setNext(List<BPlusTreeNode> values) {
            mNext = new ArrayList<>();
            mNext.addAll(values);
        }

        /**
         * Get next tree node with corresponding index.
         *
         * @param index index to get.
         * @return a tree node with corresponding index.
         */
        public BPlusTreeNode getNext(int index) {
            return mNext.get(index);
        }

        /**
         * Get all next tree nodes in this tree node.
         *
         * @return a list of tree nodes.
         */
        public ArrayList<BPlusTreeNode> getNexts() {
            return mNext;
        }

        /**
         * Check whether this tree node is empty.
         * That is, no key stored in this tree node.
         *
         * @return true if empty, false otherwise.
         */
        public boolean isEmpty() {
            return mKeys.isEmpty();
        }

        /**
         * Check whether this tree node is full.
         * That is, number of keys is equal to or greater than order.
         *
         * @return true if full, false otherwise.
         */
        public boolean isFull() {
            return mKeys.size() >= mOrder;
        }

        @Override
        public String toString() {
            return "BPlusTreeNode{" +
                           "mKeys=" + mKeys +
                           ", values=" + mValues +
                           '}';
        }

        /**
         * Get the q part when split.
         *
         * @return a TreeNode with datas belongs to q.
         */
        private BPlusTreeNode splitRight() {
            int midIndex = mKeys.size() / 2;
            BPlusTreeNode result = new BPlusTreeNode();
            result.setKeys(mKeys.subList(midIndex + 1, mKeys.size()));
            result.setValues(mValues.subList(midIndex + 1, mValues.size()));
            result.setNext(mNext.subList(midIndex + 1, mNext.size()));
            return result;
        }
    }

    /**
     * B Plus Tree Find Result.
     * A type returned by get().
     */
    private class BPlusTreeFindResult {
        public BPlusTreeNode mResult;
        public int mIndex;

        /**
         * Initialize.
         *
         * @param result result tree node.
         * @param index index of the key value in result tree node.
         */
        public BPlusTreeFindResult(BPlusTreeNode result, int index) {
            mResult = result;
            mIndex = index;
        }

        /**
         * Result getter.
         *
         * @return result tree node.
         */
        public BPlusTreeNode getResult() {
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
    private int mCapacity;
    private int mSize;
    private BPlusTreeNode mRoot;
    private BPlusTreeData mFirst;
    private BPlusTreeData mLast;

    private final Comparator<? super K> mComparator;

    /**
     * Initialize a b plus tree.
     *
     * @param order tree order.
     *              Need >= 3.
     * @param capacity tree data block capacity.
     *                 Need >= 3.
     */
    public BPlusTree(int order, int capacity) {
        if (order < 3) {
            order = 3;
        }
        if (capacity < 3) {
            capacity = 3;
        }
        mOrder = order;
        mCapacity = capacity;
        mSize = 0;
        mRoot = new BPlusTreeNode();
        mRoot.setValue(0, new BPlusTreeData());
        mFirst = mRoot.getValue(0);
        mLast = mRoot.getValue(0);
        mComparator = (o1, o2) -> ((Comparable) o1).compareTo(o2);
    }

    /**
     * Initialize a b plus tree.
     *
     * @param order tree order.
     *              Need >= 3.
     * @param capacity tree data block capacity.
     *                 Need >= 3.
     * @param comparator comparator.
     */
    public BPlusTree(int order, int capacity, Comparator<? super K> comparator) {
        if (order < 3) {
            order = 3;
        }
        if (capacity < 3) {
            capacity = 3;
        }
        mOrder = order;
        mCapacity = capacity;
        mSize = 0;
        mRoot = new BPlusTreeNode();
        mRoot.setValue(0, new BPlusTreeData());
        mFirst = mRoot.getValue(0);
        mLast = mRoot.getValue(0);
        mComparator = comparator;
    }

    /**
     * Initialize a b plus tree with initial data given.
     * Note that (key, value) pair need to be sort in ascending order.
     * And size of keys needs to equal to size of values.
     *
     * @param order tree order.
     *              Need >= 3.
     * @param capacity tree data block capacity.
     *                 Need >= 3.
     * @param keys a list of keys (must sorted).
     * @param values a list of values.
     */
    public BPlusTree(int order, int capacity, ArrayList<K> keys, ArrayList<V> values) {
        if (order < 3) {
            order = 3;
        }
        if (capacity < 3) {
            capacity = 3;
        }
        mOrder = order;
        mCapacity = capacity;
        mComparator = (o1, o2) -> ((Comparable) o1).compareTo(o2);
        construct(keys, values);
    }

    /**
     * Initialize a b plus tree with initial data given.
     * Note that (key, value) pair need to be sort in ascending order.
     * And size of keys needs to equal to size of values.
     *
     * @param order tree order.
     *              Need >= 3.
     * @param capacity tree data block capacity.
     *                 Need >= 3.
     * @param comparator comparator.
     * @param keys a list of keys (must sorted).
     * @param values a list of values.
     */
    public BPlusTree(int order, int capacity, Comparator<? super K> comparator, ArrayList<K> keys, ArrayList<V> values) {
        if (order < 3) {
            order = 3;
        }
        if (capacity < 3) {
            capacity = 3;
        }
        mOrder = order;
        mCapacity = capacity;
        mComparator = comparator;
        construct(keys, values);
    }

    /**
     * Construct a new tree with keys and values given.
     * Note that (key, value) pair need to be sort in ascending order.
     * And size of keys needs to equal to size of values.
     *
     * @param keys a list of keys (must sorted).
     * @param values a list of values.
     */
    public void construct(ArrayList<K> keys, ArrayList<V> values) {
        mSize = 0;
        mRoot = new BPlusTreeNode();
        mRoot.setValue(0, new BPlusTreeData());
        mFirst = mRoot.getValue(0);
        mLast = mRoot.getValue(0);
        int inputSize = keys.size();
        for (int i = 0; i < inputSize; ++i) {
            mSize += mLast.putLast(keys.get(i), values.get(i));
            if (mLast.isFull()) {
                K midKey = mLast.getMidKey();
                BPlusTreeData newData = mLast.split();
                insertTreeNode(mRoot, null, midKey, newData);
                mLast = newData;
            }
        }
    }

    /**
     * Put data pair (key, value) into this tree.
     *
     * @param key key to insert.
     * @param value value to insert.
     */
    public void put(K key, V value) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, key);
        int index = Collections.binarySearch(treeNode.getKeys(), key, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        BPlusTreeData treeData = treeNode.getValue(index);
        mSize += treeData.put(key, value);
        if (treeData.isFull()) {
            K midKey = treeData.getMidKey();
            BPlusTreeData newData = treeData.split();
            insertTreeNode(mRoot, null, midKey, newData);
            if (mLast == treeData) {
                mLast = newData;
            }
        }
    }

    /**
     * Get value with corresponding key.
     *
     * @param key key to get.
     * @return value with corresponding key.
     */
    public V get(K key) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, key);
        int index = Collections.binarySearch(treeNode.getKeys(), key, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        BPlusTreeData treeData = treeNode.getValue(index);
        return treeData.get(key);
    }

    /**
     * Get all keys.
     *
     * @return a list of keys.
     */
    public ArrayList<K> getKeys() {
        ArrayList<K> ans = new ArrayList<>();
        BPlusTreeData treeData = mFirst;
        while (treeData != null) {
            ans.addAll(treeData.getKeys());
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all keys in range [fromKey, toKey).
     *
     * @param fromKey lower bound (inclusive) key value.
     * @param toKey upper bound (exclusive) key value.
     * @return a list of keys in range [fromKey, toKey).
     */
    public ArrayList<K> getKeys(K fromKey, K toKey) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, fromKey);
        int index = Collections.binarySearch(treeNode.getKeys(), fromKey, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        ArrayList<K> ans = new ArrayList<>();
        BPlusTreeData treeData = treeNode.getValue(index);
        while (treeData != null) {
            if (treeData.size() > 0 && mComparator.compare(toKey, treeData.getKey(0)) <= 0) {
                return ans;
            }
            int startIndex = Collections.binarySearch(treeData.getKeys(), fromKey, mComparator);
            if (startIndex < 0) {
                startIndex = (-1) * startIndex - 1;
            }
            int endIndex = Collections.binarySearch(treeData.getKeys(), toKey, mComparator);
            if (endIndex < 0) {
                endIndex = (-1) * endIndex - 1;
            }
            ans.addAll(treeData.getKeys().subList(startIndex, endIndex));
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get keys in range given.
     *
     * @param fromKey lower bound key value.
     * @param fromInclusive true if lower bound inclusive, false otherwise.
     * @param toKey upper bound key value.
     * @param toInclusive true if upper bound inclusive, false otherwise.
     * @return a list of keys in specified range.
     */
    public ArrayList<K> getKeys(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, fromKey);
        int index = Collections.binarySearch(treeNode.getKeys(), fromKey, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        ArrayList<K> ans = new ArrayList<>();
        BPlusTreeData treeData = treeNode.getValue(index);
        while (treeData != null) {
            if (toInclusive && treeData.size() > 0 && mComparator.compare(toKey, treeData.getKey(0)) < 0) {
                return ans;
            }
            if (!toInclusive && treeData.size() > 0 && mComparator.compare(toKey, treeData.getKey(0)) <= 0) {
                return ans;
            }
            int startIndex = Collections.binarySearch(treeData.getKeys(), fromKey, mComparator);
            if (startIndex < 0) {
                startIndex = (-1) * startIndex - 1;
            } else {
                if (!fromInclusive) {
                    ++startIndex;
                }
            }
            int endIndex = Collections.binarySearch(treeData.getKeys(), toKey, mComparator);
            if (endIndex < 0) {
                endIndex = (-1) * endIndex - 1;
            } else {
                if (toInclusive) {
                    ++endIndex;
                }
            }
            ans.addAll(treeData.getKeys().subList(startIndex, endIndex));
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all values.
     *
     * @return a list of values.
     */
    public ArrayList<V> getValues() {
        ArrayList<V> ans = new ArrayList<>();
        BPlusTreeData treeData = mFirst;
        while (treeData != null) {
            ans.addAll(treeData.getValues());
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all values in range [fromKey, toKey).
     *
     * @param fromKey lower bound(inclusive) key value.
     * @param toKey upper bound(exclusive) key value.
     * @return a list of all values in range [fromKey, toKey).
     */
    public ArrayList<V> getValues(K fromKey, K toKey) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, fromKey);
        int index = Collections.binarySearch(treeNode.getKeys(), fromKey, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        ArrayList<V> ans = new ArrayList<>();
        BPlusTreeData treeData = treeNode.getValue(index);
        while (treeData != null) {
            if (treeData.size() > 0 && mComparator.compare(toKey, treeData.getKey(0)) <= 0) {
                return ans;
            }
            int startIndex = Collections.binarySearch(treeData.getKeys(), fromKey, mComparator);
            if (startIndex < 0) {
                startIndex = (-1) * startIndex - 1;
            }
            int endIndex = Collections.binarySearch(treeData.getKeys(), toKey, mComparator);
            if (endIndex < 0) {
                endIndex = (-1) * endIndex - 1;
            }
            ans.addAll(treeData.getValues().subList(startIndex, endIndex));
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get values in range given.
     *
     * @param fromKey lower bound key value.
     * @param fromInclusive true if lower bound inclusive, false otherwise.
     * @param toKey upper bound key value.
     * @param toInclusive true if upper bound inclusive, false otherwise.
     * @return a list of values in specified range.
     */
    public ArrayList<V> getValues(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, fromKey);
        int index = Collections.binarySearch(treeNode.getKeys(), fromKey, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        ArrayList<V> ans = new ArrayList<>();
        BPlusTreeData treeData = treeNode.getValue(index);
        while (treeData != null) {
            if (toInclusive && treeData.size() > 0 && mComparator.compare(toKey, treeData.getKey(0)) < 0) {
                return ans;
            }
            if (!toInclusive && treeData.size() > 0 && mComparator.compare(toKey, treeData.getKey(0)) <= 0) {
                return ans;
            }
            int startIndex = Collections.binarySearch(treeData.getKeys(), fromKey, mComparator);
            if (startIndex < 0) {
                startIndex = (-1) * startIndex - 1;
            } else {
                if (!fromInclusive) {
                    ++startIndex;
                }
            }
            int endIndex = Collections.binarySearch(treeData.getKeys(), toKey, mComparator);
            if (endIndex < 0) {
                endIndex = (-1) * endIndex - 1;
            } else {
                if (toInclusive) {
                    ++endIndex;
                }
            }
            ans.addAll(treeData.getValues().subList(startIndex, endIndex));
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all keys where key value < key given.
     *
     * @param key upper bound(exclusive) key value.
     * @return a list of keys where key value < key given.
     */
    public ArrayList<K> getKeysLess(K key) {
        ArrayList<K> ans = new ArrayList<>();
        BPlusTreeData treeData = mFirst;
        while (treeData != null) {
            if (treeData.size() > 0 && mComparator.compare(key, treeData.getKey(0)) <= 0) {
                return ans;
            }
            int startIndex = 0;
            int endIndex = Collections.binarySearch(treeData.getKeys(), key, mComparator);
            if (endIndex < 0) {
                endIndex = (-1) * endIndex - 1;
            }
            ans.addAll(treeData.getKeys().subList(startIndex, endIndex));
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all values where key value < key given.
     *
     * @param key upper bound(exclusive) key value.
     * @return a list of values where key value < key given.
     */
    public ArrayList<V> getValuesLess(K key) {
        ArrayList<V> ans = new ArrayList<>();
        BPlusTreeData treeData = mFirst;
        while (treeData != null) {
            if (treeData.size() > 0 && mComparator.compare(key, treeData.getKey(0)) <= 0) {
                return ans;
            }
            int startIndex = 0;
            int endIndex = Collections.binarySearch(treeData.getKeys(), key, mComparator);
            if (endIndex < 0) {
                endIndex = (-1) * endIndex - 1;
            }
            ans.addAll(treeData.getValues().subList(startIndex, endIndex));
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all keys where key value <= key given.
     *
     * @param key upper bound(inclusive) key value.
     * @return a list of keys where key value <= key given.
     */
    public ArrayList<K> getKeysLessEqual(K key) {
        ArrayList<K> ans = new ArrayList<>();
        BPlusTreeData treeData = mFirst;
        while (treeData != null) {
            if (treeData.size() > 0 && mComparator.compare(key, treeData.getKey(0)) < 0) {
                return ans;
            }
            int startIndex = 0;
            int endIndex = Collections.binarySearch(treeData.getKeys(), key, mComparator);
            if (endIndex < 0) {
                endIndex = (-1) * endIndex - 1;
            } else {
                ++endIndex;
            }
            ans.addAll(treeData.getKeys().subList(startIndex, endIndex));
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all values where key value <= key given.
     *
     * @param key upper bound(inclusive) key value.
     * @return a list of values where key value <= key given.
     */
    public ArrayList<V> getValuesLessEqual(K key) {
        ArrayList<V> ans = new ArrayList<>();
        BPlusTreeData treeData = mFirst;
        while (treeData != null) {
            if (treeData.size() > 0 && mComparator.compare(key, treeData.getKey(0)) < 0) {
                return ans;
            }
            int startIndex = 0;
            int endIndex = Collections.binarySearch(treeData.getKeys(), key, mComparator);
            if (endIndex < 0) {
                endIndex = (-1) * endIndex - 1;
            } else {
                ++endIndex;
            }
            ans.addAll(treeData.getValues().subList(startIndex, endIndex));
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all keys where key value > key given.
     *
     * @param key lower bound (exclusive) key value.
     * @return a list of keys where key value > key given.
     */
    public ArrayList<K> getKeysGreater(K key) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, key);
        int index = Collections.binarySearch(treeNode.getKeys(), key, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        ArrayList<K> ans = new ArrayList<>();
        BPlusTreeData treeData = treeNode.getValue(index);
        int startIndex = Collections.binarySearch(treeData.getKeys(), key, mComparator);
        if (startIndex < 0) {
            startIndex = (-1) * startIndex - 1;
        } else {
            ++startIndex;
        }
        ans.addAll(treeData.getKeys().subList(startIndex, treeData.getKeys().size()));
        treeData = treeData.getNext();
        while (treeData != null) {
            ans.addAll(treeData.getKeys());
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all values where key value > key given.
     *
     * @param key lower bound (exclusive) key value.
     * @return a list of values where key value > key given.
     */
    public ArrayList<V> getValuesGreater(K key) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, key);
        int index = Collections.binarySearch(treeNode.getKeys(), key, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        ArrayList<V> ans = new ArrayList<>();
        BPlusTreeData treeData = treeNode.getValue(index);
        int startIndex = Collections.binarySearch(treeData.getKeys(), key, mComparator);
        if (startIndex < 0) {
            startIndex = (-1) * startIndex - 1;
        } else {
            ++startIndex;
        }
        ans.addAll(treeData.getValues().subList(startIndex, treeData.getValues().size()));
        treeData = treeData.getNext();
        while (treeData != null) {
            ans.addAll(treeData.getValues());
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all keys where key value >= key given.
     *
     * @param key lower bound (inclusive) key value.
     * @return a list of keys where key value >= key given.
     */
    public ArrayList<K> getKeysGreaterEqual(K key) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, key);
        int index = Collections.binarySearch(treeNode.getKeys(), key, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        ArrayList<K> ans = new ArrayList<>();
        BPlusTreeData treeData = treeNode.getValue(index);
        int startIndex = Collections.binarySearch(treeData.getKeys(), key, mComparator);
        if (startIndex < 0) {
            startIndex = (-1) * startIndex - 1;
        }
        ans.addAll(treeData.getKeys().subList(startIndex, treeData.getKeys().size()));
        treeData = treeData.getNext();
        while (treeData != null) {
            ans.addAll(treeData.getKeys());
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Get all values where key value >= key given.
     *
     * @param key lower bound (inclusive) key value.
     * @return a list of values where key value >= key given.
     */
    public ArrayList<V> getValuesGreaterEqual(K key) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, key);
        int index = Collections.binarySearch(treeNode.getKeys(), key, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        ArrayList<V> ans = new ArrayList<>();
        BPlusTreeData treeData = treeNode.getValue(index);
        int startIndex = Collections.binarySearch(treeData.getKeys(), key, mComparator);
        if (startIndex < 0) {
            startIndex = (-1) * startIndex - 1;
        }
        ans.addAll(treeData.getValues().subList(startIndex, treeData.getValues().size()));
        treeData = treeData.getNext();
        while (treeData != null) {
            ans.addAll(treeData.getValues());
            treeData = treeData.getNext();
        }
        return ans;
    }

    /**
     * Check whether there is key in this tree.
     *
     * @param key key to check.
     * @return true it this tree contains the key, false otherwise.
     */
    public boolean containsKey(K key) {
        BPlusTreeNode treeNode = findTreeNode(mRoot, null, key);
        int index = Collections.binarySearch(treeNode.getKeys(), key, mComparator);
        if (index < 0) {
            index = (-1) * index - 1;
        } else {
            ++index;
        }
        BPlusTreeData treeData = treeNode.getValue(index);
        return treeData.get(key) != null;
    }

    /**
     * Check whether there is value in this tree.
     * ** NEED TRAVERSE WHOLE TREE **
     *
     * @param value value to check.
     * @return true if this tree contains the value, false otherwise.
     */
    public boolean containsValue(V value) {
        ArrayList<V> allValues = getValues();
        for (V v : allValues) {
            if (v.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get size of this tree.
     * That is, number of data in this tree.
     *
     * @return size of this tree.
     */
    public int size() {
        return mSize;
    }

    /**
     * Check whether this tree is empty.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return mSize == 0;
    }

    /**
     * Get first data block.
     * (The first block of linked list).
     *
     * @return the first tree data block.
     */
    private BPlusTreeData getFirstDataBlock() {
        return mFirst;
    }

    /**
     * Find tree node contains key given.
     *
     * @param root root node.
     * @param parent parent node.
     * @param key key to search.
     * @return a find result.
     */
    private BPlusTreeNode findTreeNode(BPlusTreeNode root, BPlusTreeNode parent, K key) {
        if (root == null) {
            return parent;
        }
        if (root.isEmpty()) {
            return root;
        }
        int index = Collections.binarySearch(root.getKeys(), key, mComparator);
        if (index >= 0) {
            return findTreeNode(root.getNext(index + 1), root, key);
        } else {
            return findTreeNode(root.getNext((-1) * index - 1), root, key);
        }
    }

    /**
     * Insert a tree node.
     *
     * @param root root node.
     * @param parent parent node.
     * @param key key to insert.
     * @param treeData data block to insert.
     */
    private void insertTreeNode(BPlusTreeNode root, BPlusTreeNode parent, K key, BPlusTreeData treeData) {
        if (root == null) {
            parent.put(key, treeData, null);
            return;
        }
        int index = Collections.binarySearch(root.getKeys(), key, mComparator);
        if (index >= 0) {
            root.setValue(index, treeData);
        } else {
            insertTreeNode(root.getNext((-1) * index - 1), root, key, treeData);
            if (root.isFull()) {
                K keyElement = root.getMidKey();
                BPlusTreeNode q = root.split();
                if (parent == null) {
                    parent = new BPlusTreeNode();
                    parent.setNext(0, root);
                    parent.put(keyElement, null, q);
                    mRoot = parent;
                } else {
                    parent.put(keyElement, null, q);
                }
            }
        }
    }

    /**
     * Dump this tree.
     * For debugging.
     */
    public void dump() {
        dump(mRoot);
        System.out.println();
    }

    /**
     * Dump this tree.
     * For debugging.
     *
     * @param root root tree node.
     */
    private void dump(BPlusTreeNode root) {
        System.out.print("(");
        if (root == null) {
            System.out.print(")");
            return;
        }
        System.out.print(root.getValue(0) + " ");
        for (int i = 0; i < root.getKeys().size(); ++i) {
            System.out.print(root.getKey(i) + " ");
            System.out.print(root.getValue(i + 1) + " ");
        }
        for (BPlusTreeNode node : root.getNexts()) {
            dump(node);
        }
        System.out.print(")");
    }
}

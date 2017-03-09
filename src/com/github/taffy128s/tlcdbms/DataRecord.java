package com.github.taffy128s.tlcdbms;

import java.util.ArrayList;

/**
 * Class for storing a tuple of data.
 * Ex. used to store ('John', 'Male', 20)
 *
 * Use append(), update() to maintain data stored.
 */
public class DataRecord {
    private ArrayList<Object> mDataList;

    /**
     * Initialize a new data record.
     */
    public DataRecord() {
        mDataList = new ArrayList<>();
    }

    /**
     * Initialize a new data record with a list of data.
     *
     * @param datas an array list of data
     */
    public DataRecord(ArrayList<Object> datas) {
        mDataList = datas;
    }

    /**
     * Append a new data into data record.<br>
     * ** NEED TO CALL WITH RIGHT ORDER. **<br>
     * For example,<br>
     * <code>
     *     dataRecord.append("John");<br>
     *     dataRecord.append("Male");<br>
     *     dataRecord.append(20);<br>
     * </code>
     *
     * @param data data to be inserted
     */
    public void append(Object data) {
        mDataList.add(data);
    }

    /**
     * Update data with column index given.<br>
     *
     * For example,<br>
     * <code>
     *     dataRecord.update("John Sena");<br>
     *     dataRecord.update("Female");<br>
     *     dataRecord.update(21);<br>
     * </code>
     *
     * @param index index to update
     * @param data new data to be inserted
     */
    public void update(int index, Object data) {
        mDataList.set(index, data);
    }

    /**
     * Clear all data stored.
     */
    public void clear() {
        mDataList.clear();
    }

    /**
     * Update data with column index given.<br>
     *
     * @param index index to update
     * @param data new data to be inserted
     */
    public void set(int index, Object data) {
        mDataList.set(index, data);
    }

    /**
     * Get data with column index given.
     *
     * @param index index to get
     * @return object with corresponding index
     */
    public Object get(int index) {
        return mDataList.get(index);
    }

    /**
     * Get all data fields in this data record.
     *
     * @return an array of all data fields
     */
    public Object[] getAllFields() {
        return mDataList.toArray();
    }

    /**
     * Get the length of this data record.
     * i.e. How many columns in the data record.
     *
     * @return length of this data record
     */
    public int length() {
        return mDataList.size();
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object item : mDataList) {
            stringBuilder.append(item);
            stringBuilder.append(" ");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
}

package com.github.taffy128s.tlcdbms;

import java.util.ArrayList;

/**
 * Class for storing a tuple of data.
 * Ex. used to store ('John', 'Male', 20).
 * <bold>Note that data can be null.</bold>
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
     * @param datas an array list of data.
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
     * @param data data to be inserted.
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
     * @param index index to update.
     * @param data new data to update.
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
     * @param index index to update.
     * @param data new data to be inserted.
     */
    public void set(int index, Object data) {
        mDataList.set(index, data);
    }

    /**
     * Get data with column index given.
     *
     * @param index index to get.
     * @return data with corresponding index.
     */
    public Object get(int index) {
        return mDataList.get(index);
    }

    /**
     * Get all data fields in this data record.<br>
     * ** MAY HAVE NULL INSIDE **
     *
     * @return an array list of all data fields.
     */
    public ArrayList<Object> getAllFields() {
        return mDataList;
    }

    /**
     * Get all data fields in this data record.
     * Replace all null items with varchar "null" to display.
     *
     * @return an array list of all data fields.
     */
    public ArrayList<Object> getAllFieldsForOutput() {
        ArrayList<Object> result = new ArrayList<>();
        for (Object data : mDataList) {
            if (data != null) {
                result.add(data);
            } else {
                result.add("null");
            }
        }
        return result;
    }

    /**
     * Get the length of this data record.
     * i.e. How many columns in the data record.
     *
     * @return length of this data record.
     */
    public int length() {
        return mDataList.size();
    }

    /**
     * Override Object.equals().
     *
     * @param o that.
     * @return true if equal, false if not.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataRecord that = (DataRecord) o;

        return mDataList != null ? mDataList.equals(that.mDataList) : that.mDataList == null;
    }

    /**
     * Override Object.hashCode().
     *
     * @return hashcode.
     */
    @Override
    public int hashCode() {
        return mDataList != null ? mDataList.hashCode() : 0;
    }

    /**
     * Override Object.toString().
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object item : mDataList) {
            stringBuilder.append(item);
            stringBuilder.append(", ");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
}

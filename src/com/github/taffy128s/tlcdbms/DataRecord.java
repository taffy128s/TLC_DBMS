package com.github.taffy128s.tlcdbms;

import java.util.ArrayList;

/**
 * Class for storing a tuple of data.
 * ex. used to store ('John', 'Male', 20)
 *
 * Use append(), update() to maintain data stored.
 */
public class DataRecord {
    private ArrayList<Object> datas;

    /**
     * Initialize a new data record.
     */
    public DataRecord() {
        datas = new ArrayList<>();
    }

    /**
     * Append a new data into data record.
     * ** NEED TO CALL WITH RIGHT ORDER. **
     *
     * @param data data to be inserted
     * @return true if succeed, false if failed
     *
     * For example,
     * <code>
     *     dataRecord.append("John");
     *     dataRecord.append("Male");
     *     dataRecord.append(20);
     * </code>
     */
    public boolean append(Object data) {
        datas.add(data);
        return true;
    }

    /**
     * Update data with column index given.
     *
     * @param index index to update
     * @param data new data to be inserted
     * @return true if succeed, false if failed
     *
     * For example,
     * <code>
     *     dataRecord.update("John Sena");
     *     dataRecord.update("Female");
     *     dataRecord.update(21);
     * </code>
     */
    public boolean update(int index, Object data) {
        datas.set(index, data);
        return true;
    }

    /**
     * Get the length of this data record.
     * i.e. How many columns in the data record.
     *
     * @return length of this data record
     */
    public int length() {
        return datas.size();
    }
}

package com.github.taffy128s.tlcdbms;

import java.io.*;
import java.util.ArrayList;

/**
 * Class for storing a tuple of data.
 * Ex. used to store ('John', 'Male', 20).
 * <b>Note that data can be null.</b>
 *
 * Use append(), clear() to maintain data stored.
 */
public class DataRecord implements DiskWritable, StringWritable {
    private ArrayList<Object> mDataList;

    /**
     * Initialize a new data record.
     */
    public DataRecord() {
        mDataList = new ArrayList<>();
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
     * Append a list of new data into data record.
     *
     * @param data a list of data to be inserted.
     */
    public void appendAll(ArrayList<Object> data) {
        mDataList.addAll(data);
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

    @Override
    public int hashCode() {
        return mDataList != null ? mDataList.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (Object item : mDataList) {
            stringBuilder.append(item);
            stringBuilder.append(", ");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public boolean writeToDisk(String filename) {
        try {
            FileWriter writer = new FileWriter(filename);
            for (int i = 0; i < mDataList.size(); ++i) {
                if (i > 0) {
                    writer.write("\0");
                }
                if (mDataList.get(i) == null) {
                    writer.write("null");
                } else {
                    writer.write(mDataList.get(i).toString());
                }
            }
            writer.close();
            return true;
        } catch (IOException e) {
            System.err.println(filename + ": I/O error.");
        }
        return false;
    }

    @Override
    public boolean restoreFromDisk(String filename) {
        try {
            mDataList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String input;
            while ((input = reader.readLine()) != null) {
                String[] datas = input.split("\0");
                for (String data : datas) {
                    if (data.equalsIgnoreCase("null")) {
                        mDataList.add(null);
                    } else if (DataChecker.isValidInteger(data)) {
                        mDataList.add(Integer.parseInt(data));
                    } else {
                        mDataList.add(data);
                    }
                }
            }
            reader.close();
            return true;
        } catch (FileNotFoundException e) {
            System.err.println(filename + ": no such file or directory.");
        } catch (IOException e) {
            System.err.println(filename + ": I/O error.");
        }
        return false;
    }

    @Override
    public String writeToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mDataList.size(); ++i) {
            if (i > 0) {
                stringBuilder.append("\0");
            }
            if (mDataList.get(i) == null) {
                stringBuilder.append("null");
            } else {
                stringBuilder.append(mDataList.get(i).toString());
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean restoreFromString(String string) {
        String[] datas = string.split("\0");
        for (String data : datas) {
            if (data.equalsIgnoreCase("null")) {
                mDataList.add(null);
            } else if (DataChecker.isValidInteger(data)) {
                mDataList.add(Integer.parseInt(data));
            } else {
                mDataList.add(data);
            }
        }
        return true;
    }

    public static int compare(DataRecord a, DataRecord b, ArrayList<Integer> indices) {
        for (int index : indices) {
            int cmp = compare(a.get(index), b.get(index));
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private static int compare(Object a, Object b) {
        if (a == null && b == null) {
            return 0;
        } else if (a != null && b == null) {
            return 1;
        } else if (a == null) {
            return -1;
        } else {
            return ((Comparable) a).compareTo(b);
        }
    }
}

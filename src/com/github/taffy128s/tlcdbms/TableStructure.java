package com.github.taffy128s.tlcdbms;

/**
 * Created by littlebird on 2017/03/18.
 */
public class TableStructure {
    private int mColumnIndex;
    private TableStructType mIndexType;

    public TableStructure(int columnIndex, TableStructType indexType) {
        mColumnIndex = columnIndex;
        mIndexType = indexType;
    }

    public int getIndex() {
        return mColumnIndex;
    }

    public TableStructType getType() {
        return mIndexType;
    }
}

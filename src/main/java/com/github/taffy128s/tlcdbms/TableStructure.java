package com.github.taffy128s.tlcdbms;

/**
 * Table structure. Record index type of column.
 */
public class TableStructure {
    private int mColumnIndex;
    private TableStructType mIndexType;

    /**
     * Constructor.
     *
     * @param columnIndex column index.
     * @param indexType index type (tree or hash).
     */
    public TableStructure(int columnIndex, TableStructType indexType) {
        mColumnIndex = columnIndex;
        mIndexType = indexType;
    }

    /**
     * Index getter.
     *
     * @return column index.
     */
    public int getIndex() {
        return mColumnIndex;
    }

    /**
     * Index type getter.
     *
     * @return index type.
     */
    public TableStructType getType() {
        return mIndexType;
    }
}

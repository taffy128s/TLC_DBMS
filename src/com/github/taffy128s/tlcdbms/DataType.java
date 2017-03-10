package com.github.taffy128s.tlcdbms;

/**
 * DataType, such as
 * INT or VARCHAR with limited length (up to 40).
 */
public class DataType {
    private DataTypeIdentifier mType;
    private int mLimit;

    /**
     * Initialize with type and limitation.
     * limit: any value is ok if type is INT.
     *
     * @param typeIdentifier type, INT or VARCHAR.
     * @param limit varchar length limit.
     */
    public DataType(DataTypeIdentifier typeIdentifier, int limit) {
        mType = typeIdentifier;
        mLimit = limit;
    }

    /**
     * Get type.
     *
     * @return data type.
     */
    public DataTypeIdentifier getType() {
        return mType;
    }

    /**
     * Get varchar length limitation.
     *
     * @return varchar length limitation.
     */
    public int getLimit() {
        return mLimit;
    }

    /**
     * Override Object.toString().
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        if (mType == DataTypeIdentifier.INT) {
            return "INT";
        } else {
            return "VARCHAR(" + mLimit + ")";
        }
    }
}

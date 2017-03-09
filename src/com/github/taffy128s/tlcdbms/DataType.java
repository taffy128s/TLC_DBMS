package com.github.taffy128s.tlcdbms;

/**
 * DataType
 * INT or VARCHAR with limited length
 */
public class DataType {
    private DataTypeIdentifier mType;
    private int mLimit;

    /**
     * Initialize with type and limitation.
     * limit: any value is ok if type is INT.
     *
     * @param typeIdentifier type, INT or VARCHAR
     * @param limit varchar length limit
     */
    public DataType(DataTypeIdentifier typeIdentifier, int limit) {
        mType = typeIdentifier;
        mLimit = limit;
    }

    /**
     * Get type
     *
     * @return data type
     */
    public DataTypeIdentifier getType() {
        return mType;
    }

    /**
     * Get varchar length limitation
     * @return varchar length limitation
     */
    public int getLimit() {
        return mLimit;
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
        if (mType == DataTypeIdentifier.INT) {
            return "INT";
        } else {
            return "VARCHAR(" + mLimit + ")";
        }
    }
}

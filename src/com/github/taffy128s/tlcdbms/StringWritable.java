package com.github.taffy128s.tlcdbms;

/**
 * String Writable Interface.
 * Support to export a object to a string, and restore from it.
 */
public interface StringWritable {
    /**
     * Write to string.
     * Note that writeToString() and restoreFromString() should be invertible
     * to each other.
     * That is,<br>
     * <code>newObject.restoreFromString(object.writeToString());</code><br>
     * should generate a new object that newObject.equals(object) == true.
     *
     * @return a string of this object.
     */
    String writeToString();

    /**
     * Restore from string.
     * Note that writeToString() and restoreFromString() should be invertible
     * to each other.
     * That is,<br>
     * <code>newObject.restoreFromString(object.writeToString());</code><br>
     * should generate a new object that newObject.equals(object) == true.
     *
     * @param string string to restore.
     * @return true if succeed, false otherwise.
     */
    boolean restoreFromString(String string);
}

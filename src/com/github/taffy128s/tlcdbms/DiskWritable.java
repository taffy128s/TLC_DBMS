package com.github.taffy128s.tlcdbms;

/**
 * Disk Writable Interface.
 * Support write object to disk, and restore it from disk.
 */
public interface DiskWritable {
    /**
     * Write object to disk.
     * Note that writeToDisk() and restoreFromDisk() should be invertible
     * to each other.
     * That is,<br>
     * <code>object.writeToDisk(SOMEFILE);<br>
     * newObject.restoreFromDisk(SOMEFILE);<br></code>
     * should generate a new object that newObject.equals(object) == true.<br>
     *
     * @param filename filename to write.
     * @return true if succeed, false otherwise.
     */
    boolean writeToDisk(String filename);

    /**
     * Read object from disk.
     * Note that writeToDisk() and restoreFromDisk() should be invertible
     * to each other.
     * That is,<br>
     * <code>object.writeToDisk(SOMEFILE);<br>
     * newObject.restoreFromDisk(SOMEFILE);<br></code>
     * should generate a new object that newObject.equals(object) == true.<br>
     *
     * @param filename filename to read.
     * @return true if succeed, false otherwise.
     */
    boolean restoreFromDisk(String filename);
}

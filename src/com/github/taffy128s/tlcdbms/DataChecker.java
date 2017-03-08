package com.github.taffy128s.tlcdbms;

/**
 * Check whether the data is valid or not.
 */
public class DataChecker {
    /**
     * Check whether data is a valid INT.
     *
     * @param data data to check
     * @return true if valid, false if invalid
     */
    public static boolean isValidInteger(String data) {
        try {
            if (data.length() >= 15)  {
                return false;
            }
            long result = Long.parseLong(data);
            return result <= 2147483647 && result >= -2147483648;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check whether data is a valid VARCHAR
     *
     * @param data data to check
     * @param limit varchar length limitation
     * @return true if valid, false if invalid
     */
    public static boolean isValidVarChar(String data, int limit) {
        return data.length() <= limit;
    }
}

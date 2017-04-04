package com.github.taffy128s.tlcdbms;

/**
 * Check whether the data is valid or not.
 */
public class DataChecker {
    /**
     * Check whether data is a "null" string.
     *
     * @param data string to check.
     * @return true if equalsIgnoreCase("null"), false otherwise.
     */
    public static boolean isStringNull(String data) {
        return data.equalsIgnoreCase("null");
    }

    /**
     * Check whether data is a valid INT.
     *
     * @param data data to check.
     * @return true if valid, false if invalid.
     */
    public static boolean isValidInteger(String data) {
        if (data == null) {
            return true;
        }
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
     * Checker varchar limitation validation.
     *
     * @param data data to check.
     * @return true if valid, false if invalid.
     */
    public static boolean isValidVarCharLimitation(String data) {
        try {
            if (data.length() >= 15) {
                return false;
            }
            long result = Long.parseLong(data);
            return result > 0 && result <= 40;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check whether data is a valid VARCHAR.
     *
     * @param data data to check.
     * @param limit varchar length limitation.
     * @return true if valid, false if invalid.
     */
    public static boolean isValidVarChar(String data, int limit) {
        return data == null || data.length() <= limit;
    }

    /**
     * Check whether data is a valid quoted varchar (ex. 'hello').
     *
     * @param data data to check.
     * @return true if valid, false if invalid.
     */
    public static boolean isValidQuotedVarChar(String data) {
        if (data == null) {
            return true;
        }
        if (data.length() < 2) {
            return false;
        } else if (data.charAt(0) == '\'' && data.charAt(data.length() - 1) == '\'') {
            return true;
        } else {
            return false;
        }
    }
}

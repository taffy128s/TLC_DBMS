package com.github.taffy128s.tlcdbms.exceptions;

/**
 * Exception for primary key
 */
public class PrimaryKeyAlreadyExistsException extends Exception {
    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public PrimaryKeyAlreadyExistsException() {
        super("Primary Key already exists in table.");
    }
}

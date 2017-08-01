/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: DuplicateKeyException.java 6 2010-09-22 00:48:42Z robertorr $
 */
package suncertify.db;

/**
 * A DuplicateKeyException is thrown while attempting to create a database record
 * using a key that already exists in the database.
 * 
 * @author Robert J. Orr
 */
public class DuplicateKeyException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>DuplicateKeyException</code> without detail message.
     */
    public DuplicateKeyException() {
    }

    /**
     * Constructs an instance of <code>DuplicateKeyException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DuplicateKeyException(String msg) {
        super(msg);
    }
}

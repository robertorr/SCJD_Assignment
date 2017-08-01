/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: RecordNotFoundException.java 6 2010-09-22 00:48:42Z robertorr $
 */
package suncertify.db;

/**
 * A RecordNotFoundException is thrown when a specified record does not exist or is marked
 * as deleted in the associated database.
 * 
 * @author Robert J. Orr
 */
public class RecordNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>RecordNotFoundException</code> without detail message.
     */
    public RecordNotFoundException() {
    }

    /**
     * Constructs an instance of <code>RecordNotFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RecordNotFoundException(String msg) {
        super(msg);
    }
}

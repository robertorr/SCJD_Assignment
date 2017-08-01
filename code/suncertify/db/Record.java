/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: Record.java 6 2010-09-22 00:48:42Z robertorr $
 */
package suncertify.db;

import java.io.Serializable;

/**
 * Record class is invariant (cannot be changed after it has been created.
 * 
 * @author Robert J. Orr
 */
public class Record implements Serializable {

    private static final long serialVersionUID = 1L;
    private String _name;
    private String _location;
    private String[] _specialties;
    private String _size;
    private String _rate;
    private String _owner;

    /**
     * Constructs a Record from the given parameters.  Extra spaces at beginning and end of parameters
     * will be removed.
     * 
     * @param name The name of the subcontractor this record relates to
     * @param location The locality in which this contractor works
     * @param specialties Comma separated list of types of work this contractor can perform
     * @param size Comma separated list of types of work this contractor can perform
     * @param rate Charge per hour for the subcontractor. This field includes the currency symbol
     * @param owner The id value (an 8 digit number) of the customer who has booked this. If this field is all blanks, the record is available for sale.
     */
    public Record(String name, String location, String specialties, String size, String rate, String owner) {
        super();
        _name = name != null ? name.trim() : null;
        _location = location != null ? location.trim() : null;
        _specialties = specialties != null ? specialties.trim().split(",") : new String[0];
        _size = size != null ? size.trim() : null;
        _rate = rate != null ? rate.trim() : null;
        _owner = owner != null ? owner.trim() : null;
    }

    /**
     * Constructs a Record from the parameters contained in the given array.  Extra spaces at beginning and end of parameters
     * will be removed.
     *
     * @param fields array of parameters (must be at least 6 elements long, and only the first six elements will be used)
     */
    public Record(String[] fields) {
        super();

        if (fields == null || fields.length < 6) {
            throw new IllegalArgumentException("incorrect number of fields");
        }

        _name = fields[0] != null ? fields[0].trim() : null;
        _location = fields[1] != null ? fields[1].trim() : null;
        _specialties = fields[2] != null ? fields[2].trim().split(",") : new String[0];
        _size = fields[3] != null ? fields[3].trim() : null;
        _rate = fields[4] != null ? fields[4].trim() : null;
        _owner = fields[5] != null ? fields[5].trim() : null;
    }

    // TODO: implement toString(), hashCode(), equals()
    
    /**
     * Gets the subcontractor name.
     *
     * @return the subcontractor name
     */
    public String getName() {
        return _name;
    }

    /**
     * Gets the subcontractor city.
     *
     * @return the subcontractor city
     */
    public String getLocation() {
        return _location;
    }

    /**
     * Get the types of work performed
     *
     * @return Comma separated list of types of work this contractor can perform
     */
    public String[] getSpecialties() {
        return _specialties;
    }

    /**
     * Gets the number of workers available when this record is booked
     * @return the number of workers available when this record is booked
     */
    public String getSize() {
        return _size;
    }

    /**
     * Gets the charge per hour for the subcontractor.  This field includes the currency symbol.
     *
     * @return the charge per hour for the subcontractor
     */
    public String getRate() {
        return _rate;
    }

    /**
     * Gets the owner of this record.
     *
     * @return The id value (an 8 digit number) of the customer who has booked this. If this field is all blanks, the record is available for sale.
     */
    public String getOwner() {
        return _owner;
    }
}

/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: Data.java 12 2013-02-12 04:08:23Z robertorr $
 */
package suncertify.db;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import suncertify.Constants;

/**
 * This class is the main database access class.
 * 
 * @author Robert J. Orr
 */
public class Data implements DBMain {

    private static final int MARKER_RECORD_VALID = 0x0000;
    private static final int MARKER_RECORD_DELETED = 0x8000;
    /**
     * This map indicates which records are locked and the associated thread that holds the lock.
     */
    private static final ConcurrentMap<Integer, String> _lockMap = new ConcurrentHashMap<Integer, String>();
    /**
     * The logger for this class.
     */
    private static final Logger _log = Logger.getLogger(Data.class.getName());
    /**
     * The handle to the database file
     */
    private final String _dbFilename;
    private final long _offsetFirstRecord;
    private final long _recordLength;
    private final int _numFields;
    private final int[] _fieldLengths;
    private final List<Integer> _csvFieldIndices;

    /**
     * Basic constructor for class
     * 
     * @throws IOException if an I/O error occurs
     */
    public Data() throws IOException {
        super();

        // get the database file name from the properties file and check it
        Properties properties = new Properties();
        FileReader reader = new FileReader(Constants.PROPERTIES_FILE);
        properties.load(reader);
        _dbFilename = properties.getProperty("database.file", "");
        if (_dbFilename.isEmpty()) {
            throw new FileNotFoundException("database filename is empty!");
        }

        String namesBlob = properties.getProperty("database.fields.names", "");
        String[] specFieldNames = namesBlob.split(",");
        String lengthsBlob = properties.getProperty("database.fields.lengths", "");
        String[] specFieldLengths = lengthsBlob.split(",");
        if (specFieldLengths.length != specFieldNames.length) {
            throw new IOException(
                    "problem in properties file: number of field names does not match number of field lengths");
        }
        _numFields = specFieldNames.length;
        _fieldLengths = new int[_numFields];
        _csvFieldIndices = new ArrayList<Integer>();
        String csvFieldsBlob = properties.getProperty("database.fields.csv", "");
        if (!csvFieldsBlob.isEmpty()) {
            String[] csvFieldsArray = csvFieldsBlob.split(",");
            for (String s : csvFieldsArray) {
                for (int i = 0; i < _numFields; ++i) {
                    if (s.equals(specFieldNames[i])) {
                        _csvFieldIndices.add(i);
                        break;
                    }
                }
            }
        }

        RandomAccessFile dbFile = null;
        try {
            // open the database in "rwd" mode to force content changes to be written synchronously
            dbFile = new RandomAccessFile(_dbFilename, "rwd");

            // check the header and store the offset to the first record
            dbFile.seek(0);    // position file pointer at file beginning
            dbFile.readInt();  // read magic cookie
            _offsetFirstRecord = dbFile.readInt(); // store offset to first record
            if (dbFile.readShort() != _numFields) {  // check number of fields in each record
                throw new IOException("problem in database header: number of fields does not match database spec");
            }

            // check the record specification
            long recLength = 2; // starts with 2-byte validity marker
            for (int i = 0; i < _numFields; ++i) {
                int len = dbFile.readShort();  // read field name length
                byte[] fieldBytes = new byte[len];
                dbFile.readFully(fieldBytes);  // read field name
                String fieldName = new String(fieldBytes);
                if (!specFieldNames[i].equals(fieldName)) { // check field name against spec
                    throw new IOException("problem in database header: field name does not match database spec: "
                            + fieldName);
                }
                int fieldLength = dbFile.readShort();  // read field length
                int specFieldLength = Integer.parseInt(specFieldLengths[i]);
                if (fieldLength != specFieldLength) {   // check field length against spec
                    throw new IOException("problem in database header: field length does not match database spec: "
                            + fieldName + " - " + fieldLength);
                }
                _fieldLengths[i] = fieldLength;
                recLength += fieldLength;
            }
            _recordLength = recLength;
        } finally {
            dbFile.close();
        }
    }

    /**
     * Reads a record from the file. Returns an array where each element is a record value.
     *
     * @param recNo record number to read
     * @return array where each element is a record value
     * @throws RecordNotFoundException if a specified record does not exist or is marked as deleted in the database
     */
    @Override
    public String[] read(final int recNo) throws RecordNotFoundException {
        RandomAccessFile dbFile = null;
        try {
            // make sure that current client has locked record
            this.checkLocked(recNo);

            dbFile = new RandomAccessFile(_dbFilename, "rwd");
            if (!this.recordExists(recNo, dbFile)) {
                throw new RecordNotFoundException("record " + recNo + " does not exist");
            }

            // seek past validity marker
            long offset = _offsetFirstRecord + (_recordLength * recNo);
            dbFile.seek(offset + 2);

            String[] data = new String[_numFields];
            for (int i = 0; i < _numFields; ++i) {
                byte[] bytes = new byte[_fieldLengths[i]];
                dbFile.readFully(bytes);
                String s = new String(bytes);
                data[i] = s.trim(); // trim() should strip off nulls and other whitespace
            }

            return data;
        } catch (IOException ex) {
            _log.warning(ex.toString());
            // transform into RecordNotFoundException so that we don't break the interface contract
            throw new RecordNotFoundException(ex.getMessage());
        } finally {
            this.closeCleanly(dbFile);
        }
    }

    /**
     * Modifies the fields of a record. The new value for field n appears in data[n].  If the data
     * array contains more elements than the number of fields in a record, only the first N will be used where N
     * is the number of fields in a record.
     *
     * @param recNo record number to update
     * @param data array where each element is a record value
     * @throws RecordNotFoundException if a specified record does not exist or is marked as deleted in the database
     * @throws IllegalArgumentException if the data array is null or contains fewer than the required number of fields
     */
    @Override
    public void update(final int recNo, final String[] data) throws RecordNotFoundException {
        if (data == null || data.length < _numFields) {
            throw new IllegalArgumentException("data is null or has fewer than required fields");
        }

        RandomAccessFile dbFile = null;
        try {
            // make sure that current client has locked record
            this.checkLocked(recNo);

            dbFile = new RandomAccessFile(_dbFilename, "rwd");
            if (!this.recordExists(recNo, dbFile)) {
                throw new RecordNotFoundException("record " + recNo + " does not exist");
            }

            // seek past validity marker
            long offset = _offsetFirstRecord + (_recordLength * recNo);
            dbFile.seek(offset + 2);

            // write the data
            this.writeData(data, dbFile);
        } catch (IOException ex) {
            _log.warning(ex.toString());
            // transform into RecordNotFoundException so that we don't break the interface contract
            throw new RecordNotFoundException(ex.getMessage());
        } finally {
            this.closeCleanly(dbFile);
        }
    }

    /**
     * Deletes a record, making the record number and associated disk storage available for reuse.
     *
     * @param recNo the record number to delete
     * @throws RecordNotFoundException if a specified record does not exist or is marked as deleted in the database
     */
    @Override
    public void delete(final int recNo) throws RecordNotFoundException {
        RandomAccessFile dbFile = null;
        try {
            // make sure that current client has locked record
            this.checkLocked(recNo);

            dbFile = new RandomAccessFile(_dbFilename, "rwd");
            if (!this.recordExists(recNo, dbFile)) {
                throw new RecordNotFoundException("record " + recNo + " does not exist");
            }

            // seek to validity marker
            long offset = _offsetFirstRecord + (_recordLength * recNo);
            dbFile.seek(offset);

            // 'delete' the record by changing its validity marker to 'deleted'
            dbFile.writeShort(MARKER_RECORD_DELETED);

        } catch (IOException ex) {
            _log.warning(ex.toString());
            // transform into RecordNotFoundException so that we don't break the interface contract
            throw new RecordNotFoundException(ex.getMessage());
        } finally {
            this.closeCleanly(dbFile);
        }
    }

    /**
     * Returns an array of record numbers that match the specified criteria. Field n in the database file is
     * described by criteria[n]. A null value in criteria[n] matches any field value. A non-null value in
     * criteria[n] matches any field value that begins with criteria[n]. (For example, "Fred" matches "Fred" or
     * "Freddy".)
     *
     * @param criteria array where field n in the database is described by criteria[n]
     * @return array of record numbers that match the specified criteria
     * @throws RecordNotFoundException if a specified record does not exist or is marked as deleted in the database
     * @throws IllegalArgumentException if the criteria array is null or contains fewer than the required number of fields or if one of the first two fields is null or empty
     */
    @Override
    public int[] find(final String[] criteria) throws RecordNotFoundException {
        synchronized (_lockMap) {

            if (criteria == null || criteria.length < _numFields) {
                throw new IllegalArgumentException("search criteria is null or has fewer than required fields");
            }

            boolean[] isCriterionEmpty = new boolean[criteria.length];
            for (int i = 0; i < criteria.length; ++i) {
                isCriterionEmpty[i] = (criteria[i] == null || criteria[i].trim().isEmpty());
            }

            List<Integer> recordList = new ArrayList<Integer>();
            RandomAccessFile dbFile = null;
            try {
                dbFile = new RandomAccessFile(_dbFilename, "rwd");

                // loop through all valid records and check against criteria

                long offset = _offsetFirstRecord;
                int currentRecord = 0;
                long dbLength = dbFile.length();    // length cannot change because of syncronization

                // loop until we get to the end of the file
                while (offset < dbLength) {

                    if (_lockMap.containsKey(currentRecord)) {
                        // wait until the record is unlocked
                        while (_lockMap.containsKey(currentRecord)) {
                            _lockMap.wait();
                        }
                    }

                    dbFile.seek(offset);

                    // check for deleted record
                    int marker = dbFile.readShort();
                    switch (marker) {
                        case MARKER_RECORD_VALID:
                            // TODO: can we find() more efficiently with regex?
                            // TODO: do we need other boolean matches besides AND?
                            // currently this implements boolean AND across all non-null criteria

                            // Search Algorithm:
                            //  Loop over all fields in the record
                            //      If a particular field does not start with its corresponding search criterion,
                            //          the record does not match, so move to the next record
                            //      If all fields match their corresponding search criteria, save the index
                            //
                            //      Fields that contain comma-separated values need special treatment:
                            //          We must check all the values against the corresponding search criterion
                            //          If none of the values match, the record does not match,
                            //              so move to next record
                            //
                            boolean matched = true;
                            for (int i = 0; i < _numFields; ++i) {
                                if (isCriterionEmpty[i]) {
                                    dbFile.skipBytes(_fieldLengths[i]);
                                    continue;
                                }

                                byte[] bytes = new byte[_fieldLengths[i]];
                                dbFile.readFully(bytes);
                                String s = new String(bytes).trim();
                                if (_csvFieldIndices.contains(i)) {
                                    String[] values = s.split(",");
                                    boolean subMatched = false;
                                    for (String v : values) {
                                        // if a subvalue matches, the whole field matches
                                        if (v.trim().startsWith(criteria[i].trim())) {
                                            subMatched = true;
                                            break;
                                        }
                                    }
                                    // if none of the subvalues matched, the record does not match
                                    if (!subMatched) {
                                        matched = false;
                                        break;
                                    }
                                } else {
                                    if (!s.startsWith(criteria[i].trim())) {
                                        matched = false;
                                        break;
                                    }
                                }
                            }

                            if (matched) {
                                recordList.add(currentRecord);
                            }
                            break;

                        case MARKER_RECORD_DELETED:
                            // do nothing...skip this record
                            break;

                        default:
                            throw new IOException("corrupted validity marker: " + marker);
                    }

                    currentRecord++;
                    offset = _offsetFirstRecord + (currentRecord * _recordLength);
                }

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (IOException ex) {
                _log.warning(ex.toString());
                // transform into RecordNotFoundException so that we don't break the interface contract
                throw new RecordNotFoundException(ex.getMessage());
            } finally {
                this.closeCleanly(dbFile);
            }

            // convert to int array
            int[] recordArray = new int[recordList.size()];
            for (int i = 0; i < recordArray.length; ++i) {
                recordArray[i] = recordList.get(i);
            }
            return recordArray;
        }
    }

    /**
     * Creates a new record in the database (possibly reusing a deleted entry). Inserts the given data, and
     * returns the record number of the new record.  If the data array contains more elements
     * than the number of fields in a record, only the first N will be used where N is the number of fields in a
     * record.
     *
     * @param data array where each element is a record value
     * @return the record number of the new record
     * @throws DuplicateKeyException when the first two fields of the given array match an already existing record in the database
     * @throws IllegalArgumentException if the data array is null or contains fewer than the required number of fields or if one of the first two fields is null or empty
     */
    @Override
    public int create(final String[] data) throws DuplicateKeyException {
        // 1) check to make sure that key doesn't already exist
        // 2) search for a deleted record
        // 3) if no deleted record is available, append to end of file
        // 4) write data to record

        // Synchronize on the lock map (to prevent anyone from writing until after we're done)
        // _and_ wait until any locked record is unlocked (so that any writes that are in progress when
        // we start are finished when we read the record)

        synchronized (_lockMap) {
            if (data == null || data.length < _numFields) {
                throw new IllegalArgumentException("data is null or has fewer than required fields");
            }

            if (data[0] == null || data[1] == null || data[0].trim().isEmpty() || data[1].trim().isEmpty()) {
                throw new IllegalArgumentException("one of the fields values used for the record key is null or empty");
            }

            String newKey = data[0].trim() + data[1].trim();
            int recordNum = -1;
            RandomAccessFile dbFile = null;
            try {
                dbFile = new RandomAccessFile(_dbFilename, "rwd");

                long offset = _offsetFirstRecord;
                int currentRecord = 0;
                long dbLength = dbFile.length();    // length cannot change...no other creates can occur

                // loop until we get to the end of the file (or find a duplicate key)
                while (offset < dbLength) {

                    if (_lockMap.containsKey(currentRecord)) {
                        // wait until the record is unlocked
                        while (_lockMap.containsKey(currentRecord)) {
                            _lockMap.wait();
                        }
                    }

                    dbFile.seek(offset);

                    // check for deleted record
                    int marker = dbFile.readShort();
                    switch (marker) {
                        case MARKER_RECORD_DELETED:
                            // save this record number for writing later
                            if (recordNum < 0) {
                                recordNum = currentRecord;
                            }
                            break;

                        case MARKER_RECORD_VALID:
                            // check valid record for duplicate key
                            byte[] key1Bytes = new byte[_fieldLengths[0]];
                            byte[] key2Bytes = new byte[_fieldLengths[1]];
                            dbFile.readFully(key1Bytes);
                            dbFile.readFully(key2Bytes);
                            String fullKey = (new String(key1Bytes)).trim() + (new String(key2Bytes)).trim();
                            if (newKey.equalsIgnoreCase(fullKey)) {
                                throw new DuplicateKeyException("a record with that key already exists");
                            }
                            break;

                        default:
                            throw new IOException("corrupted validity marker: " + marker);
                    }

                    currentRecord++;
                    offset = _offsetFirstRecord + (currentRecord * _recordLength);
                }

                if (recordNum < 0) {
                    recordNum = currentRecord;
                }
                offset = _offsetFirstRecord + (recordNum * _recordLength);
                dbFile.seek(offset);

                // write the data
                dbFile.writeShort(MARKER_RECORD_VALID);
                this.writeData(data, dbFile);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (IOException ex) {
                _log.warning(ex.toString());
                // transform into RuntimeException so that we don't break the interface contract
                throw new RuntimeException(ex.getMessage());
            } finally {
                this.closeCleanly(dbFile);
            }

            return recordNum;
        }
    }

    /**
     * 
     * @param data
     * @param file
     * @throws IOException
     */
    private void writeData(final String[] data, final RandomAccessFile file) throws IOException {
        assert (data != null && data.length >= _numFields);
        byte space = " ".getBytes("US-ASCII")[0];
        for (int i = 0; i < _numFields; ++i) {
            // create byte array and fill with spaces
            byte[] writeBytes = new byte[_fieldLengths[i]];
            Arrays.fill(writeBytes, space);
            if (i < data.length && data[i] != null) {
                // copy bytes from parameter string
                byte[] argBytes = data[i].getBytes("US-ASCII");
                int numBytes = Math.min(_fieldLengths[i], argBytes.length);
                for (int j = 0; j < numBytes; ++j) {
                    writeBytes[j] = argBytes[j];
                }
            }
            // write field to database file
            file.write(writeBytes);
        }
    }

    /**
     * Locks a record so that it can only be updated or deleted by this client. If the specified record is
     * already locked, the current thread gives up the CPU and consumes no CPU cycles until the record is
     * unlocked.
     *
     * @param recNo the record number to lock
     * @throws RecordNotFoundException if a specified record does not exist or is marked as deleted in the database
     */
    @Override
    public void lock(final int recNo) throws RecordNotFoundException {
        // synchronization ensures atomicity
        synchronized (_lockMap) {
            // if the record does not exist, throw RecordNotFoundException
            RandomAccessFile dbFile = null;
            try {
                dbFile = new RandomAccessFile(_dbFilename, "rwd");
                if (!this.recordExists(recNo, dbFile)) {
                    throw new RecordNotFoundException("record " + recNo + " does not exist");
                }
            } catch (IOException ex) {
                _log.warning(ex.toString());
                throw new RecordNotFoundException(ex.getMessage());
            } finally {
                this.closeCleanly(dbFile);
            }

            try {
                // wait until the record is unlocked
                while (_lockMap.containsKey(recNo)) {
                    _lockMap.wait();
                }

                // lock the record
                String clientID = Thread.currentThread().getName();
                _lockMap.put(recNo, clientID);

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Releases the lock on a record.
     *
     * @param recNo the record number to unlock
     * @throws RecordNotFoundException if a specified record does not exist or is marked as deleted in the database
     */
    @Override
    public void unlock(final int recNo) throws RecordNotFoundException {
        // synchronization ensures atomicity
        synchronized (_lockMap) {
            // if the record does not exist, throw RecordNotFoundException
            RandomAccessFile dbFile = null;
            try {
                dbFile = new RandomAccessFile(_dbFilename, "rwd");
                if (!this.recordExists(recNo, dbFile)) {
                    throw new RecordNotFoundException("record " + recNo + " does not exist");
                }
            } catch (IOException ex) {
                _log.warning(ex.toString());
                throw new RecordNotFoundException(ex.getMessage());
            } finally {
                this.closeCleanly(dbFile);
            }

            // if record is not locked, just return
            if (!_lockMap.containsKey(recNo)) {
                return;
            }

            // remove the lock only if the current thread owns it
            String clientID = Thread.currentThread().getName();
            if (_lockMap.remove(recNo, clientID)) {
                // notify all sleeping threads that a lock has been released
                // we should use notifyAll() because threads may be waiting on different record locks,
                // and if we use notify() the thread that is woken may not be waiting on this unlocked record
                _lockMap.notifyAll();
            }
        }
    }

    /**
     * Determines if a record is currently locked. Returns true if the record is locked, false otherwise.
     * 
     * @param recNo the record number to check for locking
     * @return true if record is locked, false otherwise
     * @throws RecordNotFoundException if a specified record does not exist or is marked as deleted in the database
     */
    @Override
    public boolean isLocked(final int recNo) throws RecordNotFoundException {
        // synchronization ensures atomicity
        synchronized (_lockMap) {
            RandomAccessFile dbFile = null;
            try {
                dbFile = new RandomAccessFile(_dbFilename, "rwd");
                if (!this.recordExists(recNo, dbFile)) {
                    throw new RecordNotFoundException("record " + recNo + " does not exist");
                }
            } catch (IOException ex) {
                _log.warning(ex.toString());
                // transform into RecordNotFoundException so that we don't break the interface contract
                throw new RecordNotFoundException(ex.getMessage());
            } finally {
                this.closeCleanly(dbFile);
            }

            return _lockMap.containsKey(recNo);
        }
    }

    /**
     * Unlocks all records held by a particular client.  Useful if client crashes without releasing locks.
     *
     * @param clientID ID of client whose locks should be released
     */
    public void clearLocks(String clientID) {
        synchronized (_lockMap) {
            if (_lockMap.containsValue(clientID)) {
                for (Entry<Integer, String> entry : _lockMap.entrySet()) {
                    if (clientID.equals(entry.getValue())) {
                        _lockMap.remove(entry.getKey());
                    }
                }
            }
        }
    }

    /**
     * Checks that a record is currently locked by the client associated with the current thread.
     *
     * @param recNo the record number to check for locking
     * @throws IllegalStateException if the record is not locked or if the client associated with the current thread does not hold the lock
     */
    private void checkLocked(final int recNo) {
        synchronized (_lockMap) {
            // check if record is already locked
            if (!_lockMap.containsKey(recNo)) {
                throw new IllegalStateException("requested record has not been locked");
            }

            // check to make sure requesting client holds the lock
            String clientID = Thread.currentThread().getName();
            String lockHolder = _lockMap.get(recNo);
            if (!lockHolder.equals(clientID)) {
                throw new IllegalStateException("client does not hold lock for requested record");
            }
        }
    }

    /**
     * Indicates whether a particular record exists in the database.
     *
     * @param recNo the record number to check (zero-based)
     * @return true if record exists and is valid, false otherwise
     * @throws RecordNotFoundException if record number is illegal (less than zero) or an I/O error occurs
     */
    private boolean recordExists(final int recNo, final RandomAccessFile file) throws RecordNotFoundException {
        try {
            // record numbers less than zero are invalid
            if (recNo < 0) {
                throw new RecordNotFoundException("record number is less than 0");
            }

            long offset = _offsetFirstRecord + (_recordLength * recNo);
            if (offset > file.length()) {
                return false;
            }

            file.seek(offset);
            int marker = file.readShort();
            return (marker == MARKER_RECORD_VALID);

        } catch (IOException ex) {
            // transform IOExceptions into RecordNotFoundExceptions
            _log.severe(ex.toString());
            throw new RecordNotFoundException(ex.getMessage());
        }
    }

    /**
     * Utility function that closes a Closeable and intercepts and logs the associated IOException.
     *
     * @param c Closeable object
     */
    private void closeCleanly(final Closeable c) {
        if (c == null) {
            return;
        }

        try {
            c.close();
        } catch (IOException ex) {
            _log.warning(ex.toString());
        }
    }
}

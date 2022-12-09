package BPv7.containers;

import java.io.Serializable;

/**
 * Abstract class to represent all types of administrative records
 */
public abstract class AdminRecord implements Serializable {
    /** record type, Status report exists for value of this being 1 */
    int recordType;
    /**
     * Constructor setting record type
     * @param a record type
     */
    AdminRecord(int a) { recordType = a; }
}

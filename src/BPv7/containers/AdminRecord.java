package BPv7.containers;

/**
 * Abstract class to represent all types of administrative records
 */
public abstract class AdminRecord {
    /** record type, Status report exists for value of this being 1 */
    int recordType;
    /**
     * Constructor setting record type
     * @param a record type
     */
    AdminRecord(int a) { recordType = a; }
}

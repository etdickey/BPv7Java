package BPv7.containers;

public abstract class AdminRecord {

    int recordType; /* record type, Status report exists for value of this being 1 */
    /* recordContent in type specific manner (Unsure what type specific manner means) */
    AdminRecord(int a) { //Constructor setting record type.
        recordType = a;
    }
    AdminRecord() {} //Default constructor

}

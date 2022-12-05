package BPv7.utils;

/**
 * Enum representing a bundle's status from the perspective of the BPA
 */
public enum DispatchStatus {
    /** held in buffer, haven't sent yet */
    PENDING,
    /** successfully sent via DTCP to next hop (does NOT indicate we have received a status report about it, though) */
    SENT,
    /** bundle had to be deleted, for whatever reason */
    DELETED,
    /** bundle does not exist from the perspective of the BPA */
    NONE,
}

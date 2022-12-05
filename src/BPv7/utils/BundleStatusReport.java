package BPv7.utils;

/**
 * Enum representing a bundle's status report from the perspective of the BPA
 */
public enum BundleStatusReport {
    /** Received bundle from DTCP */
    RECEIVED,
    /** Forwarded bundle to another node */
    FORWARDED,
    /** Delivered bundle to application agent */
    DELIVERED,
    /** Bundle has been deleted for some reason */
    DELETED
}

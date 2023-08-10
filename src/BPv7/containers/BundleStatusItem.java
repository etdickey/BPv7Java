package BPv7.containers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class contains the status and status report's time for a bundle for one
 *  of received, forwarded, delivered and deleted.
 * @implSpec CBOR array with length:<br>
 *     2: if the value of the first element of the bundle status item is 1 AND
 *        the "Report status time" flag was set to 1 in the bundle processing
 *        control flags of the bundle whose status is being reported
 *     1: otherwise
 */
public class BundleStatusItem {
    /**
     * For each of received, forwarded, delivered and deleted, variable is set to
     *  true if the corresponding assertion has occurred.
     * @implSpec CBOR Boolean value
     */
    boolean status;
    /**
     * if "Report status time" flag was set to 1, time at which the indicated
     *  status was asserted for this bundle. Otherwise, just null.
     * @implSpec CBOR:: see DTNTime header
     */
    DTNTime reportStatusTime = null;

    /** Constructor taking only status as parameter. */
    @JsonCreator
    public BundleStatusItem(@JsonProperty("status") boolean status) { this.status = status; }

    /** setter for reportStatusTime if "Report status time" flag was set to 1. */
    void setReportStatusTime(DTNTime a) { reportStatusTime = a; }

    public boolean getStatus() { return this.status; }
}

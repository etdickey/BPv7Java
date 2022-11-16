package BPv7.containers;

/**
 * A class containing specification for bundle status reports and all its items:
 *  Status Indicator, reason code, source bundle's nodeID, and creation time stamp.
 *  Fragment offset and length of payload if fragment offset exists (bundle is a fragment).
 *
 * @implSpec CBOR array with length:<br>
 *     6: if the bundle is a fragment<br>
 *     4: otherwise
 */
public class StatusReport extends AdminRecord{
    /**
     * Record type for StatusReport
     * ...<br>
     * probably needs to be moved to AdminRecord so we can record all admin record types in one spot...
     */
    private static final int RECORD_TYPE_STATUS_REPORT = 1;
    /*todo:: Getters and setters for status report info*/
    /**
     * Array of 4 Bundle Status Item objects, 1 each for received, forwarded, delivered and deleted.
     * @implSpec CBOR array of length >=4
     */
    BundleStatusItem[] statusIndicator = new BundleStatusItem[4];

    /**
     * An integer code explaining the reason for the status indicator values.
     * Here are the possible values:
     *     0 -> No additional information.
     *     1 -> Lifetime expired.
     *     2 -> Forwarded over unidirectional link.
     *     3 -> Transmission canceled.
     *     4 -> Depleted storage.
     *     5 -> Destination endpoint ID unavailable.
     *     6 -> No known route to destination from here.
     *     7 -> No timely contact with next node on route.
     *     8 -> Block unintelligible.
     *     9 -> Hop limit exceeded.
     *     10 -> Traffic pared (e.g., status reports).
     *     11 -> Block unsupported.
     *     17-254 -> Unassigned.
     *     255 -> Reserved.
     * @implSpec CBOR unsigned int
     */
    int reasonCode;
    /**
     * node ID identifying the source of the bundle whose status is being reported
     * @implSpec CBOR: Check NodeID header
     */
    NodeID sourceBundleNodeID;
    /**
     * creation timestamp of the bundle whose status is being reported
     * @implSpec CBOR: Check Timestamp header
     */
    Timestamp creationTimestamp;

    /**
     * subject bundle's fragment offset, present only if bundle whose status
     *   is reported contained fragment offset
     * @implSpec CBOR unsigned int
     */
    int fragmentOffset;
    /**
     * length of the subject bundle's payload, present only if bundle whose status
     *  is reported contained fragment offset
     * @implSpec CBOR unsigned int
     */
    int lengthOfPayload;

    /**
     * Constructs a status report (admin record) with the specified fields
     *  (contains all but fragmenting variables as parameters, + recordType
     *   from parent abstract class)
     * @param received received status
     * @param forwarded forwarded status
     * @param delivered delivered status
     * @param deleted deleted status
     * @param rCode reason for transmission of status report
     * @param sBundleNodeID source of the bundle whose status is being reported
     * @param cTimeStamp creation timestamp of the bundle whose status is being reported
     */
    StatusReport(BundleStatusItem received, BundleStatusItem forwarded, BundleStatusItem delivered, BundleStatusItem deleted, int rCode, NodeID sBundleNodeID, Timestamp cTimeStamp) {
        super(RECORD_TYPE_STATUS_REPORT);
        statusIndicator[0] = received;
        statusIndicator[1] = forwarded;
        statusIndicator[2] = delivered;
        statusIndicator[3] = deleted;
        reasonCode = rCode;
        sourceBundleNodeID = sBundleNodeID;
        creationTimestamp = cTimeStamp;
    }

    /** setter for fragment offset if present */
    void setFragmentOffset(int a) { fragmentOffset = a; }

    /** setter for length of payload if present */
    void setLengthOfPayload(int a) { lengthOfPayload = a; }
}

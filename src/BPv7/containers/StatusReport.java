package BPv7.containers;

/**
 *  A class containing specification for bundle status reports and all its items.
 *  Status Indicator, reason code, source bundle's nodeID, and creation time stamp.
 *  Fragment offset and length of payload if fragment offset exists.
 * */
public class StatusReport extends AdminRecord{
    /*Getters and setters for status report info*/
    BundleStatusItem statusIndicator[] = new BundleStatusItem[4]; //Array of 4 Bundle Status Item objects, 1 each for received, forwarded, delivered and deleted.
    int reasonCode; //An integer code explaining the reason for the status indicator values. Below are the possible values
    /*
    * 0 -> No additional information.
    * 1 -> Lifetime expired.
    * 2 -> Forwarded over unidirectional link.
    * 3 -> Transmission canceled.
    * 4 -> Depleted storage.
    * 5 -> Destination endpoint ID unavailable.
    * 6 -> No known route to destination from here.
    * 7 -> No timely contact with next node on route.
    * 8 -> Block unintelligible.
    * 9 -> Hop limit exceeded.
    * 10 -> Traffic pared (e.g., status reports).
    * 11 -> Block unsupported.
    * 17-254 -> Unassigned.
    * 255 -> Reserved.
     * */
    NodeID sourceBundleNodeID; //node ID identifying the source of the bundle whose status is being reported
    Timestamp creationTimestamp; //creation timestamp of the bundle whose status is being reported

    int fragmentOffset; //subject bundle's fragment offset, present only if bundle whose status is reported contained fragment offset

    int lengthOfPayload; //length of the subject bundle's payload, present only if bundle whose status is reported contained fragment offset

    StatusReport(BundleStatusItem received,BundleStatusItem forwarded,BundleStatusItem delivered,BundleStatusItem deleted, int rCode, NodeID sBundleNodeID, Timestamp cTimeStamp, int rType) {
        statusIndicator[0] = received;
        statusIndicator[1] = forwarded;
        statusIndicator[2] = delivered;
        statusIndicator[3] = deleted;
        reasonCode = rCode;
        sourceBundleNodeID = sBundleNodeID;
        creationTimestamp = cTimeStamp;
        super.recordType = rType;
    } //Constructor containing all but fragmenting variables as parameters, + recordType from parent abstract class

    void setFragmentOffset(int a) { //setter for fragment offset if present
        fragmentOffset = a;
    }

    void setLengthOfPayload(int a) { //setter for length of payload if present
        lengthOfPayload = a;
    }
}

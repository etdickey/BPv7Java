package BPv7.containers;

/**
 *  This class contains the status and status report's time for a bundle for one of received, forwarded, delivered and deleted.
 * */

public class BundleStatusItem {
    boolean status; //For each of received, forwarded, delivered and deleted, variable is set to true if the corresponding assertion has occurred.
    DTNTime reportStatusTime = null; //if "Report status time" flag was set to 1, time at which the indicated status was asserted for this bundle. Otherwise, just null.

    BundleStatusItem(boolean a) { //Constructor taking status as parameter.
        status = a;
    }

    void setReportStatusTime(DTNTime a) { //setter for reportStatusTime if "Report status time" flag was set to 1.
        reportStatusTime = a;
    }
}

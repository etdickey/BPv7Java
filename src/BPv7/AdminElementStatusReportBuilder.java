package BPv7;
import BPv7.containers.BundleStatusItem;
import BPv7.containers.StatusReport;
import BPv7.utils.BundleStatusReport;
import BPv7.utils.StatusReportUtilObject;

import java.io.IOException;
import java.util.logging.Logger;

import static BPv7.BPA.*;

public class AdminElementStatusReportBuilder implements Runnable {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(AdminElementStatusReportBuilder.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static AdminElementStatusReportBuilder instance = null;
    /**
     * todo:: comments
     */
    // private final DTCPInterface dtcp = DTCP.getInstance();


    //functions!

    /**
     * Gets the singleton instance of the BPADispatcher
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static AdminElementStatusReportBuilder getInstance() {
        if(instance == null){
            synchronized (AdminElementStatusReportBuilder.class) {
                if(instance == null) {
                    instance = new AdminElementStatusReportBuilder();
                    logger.info("Created AdminElementStatusReportBuilder singleton");
                }
            }
        }
        return instance;
    }

    protected AdminElementStatusReportBuilder() {}

    /**
     *
     */
    @Override
    public void run() {
        // passive sender thread: will spawn when we want to send a message
        // and only remain alive until all messages are sent.  More efficient
        // than having it busy-wait until a new message is ready to be sent.
        while(true) {
            try {
                StatusReportUtilObject statusReportRevd = BPA.sendStatusReportBuffer.take();

                // TODO: check if the sendStatusReportBuffer is empty
                /*
                if (statusReportRevd == null) {
                    continue; // Skip following code
                }
                else {
                    .. 
                }
                */
    
                // O - REC; 1 - FORW; 2 - DELI; 3 - DEL
                BundleStatusReport status = statusReportRevd.getBundleStatusReportEnum();
                BundleStatusItem received = new BundleStatusItem(true);
                BundleStatusItem forwarded = new BundleStatusItem((status == 
                                                 BundleStatusReport.FORWARDED) ? true : false);
                BundleStatusItem delivered = new BundleStatusItem((status == 
                                                 BundleStatusReport.DELIVERED) ? true : false);
                BundleStatusItem deleted = new BundleStatusItem((status == 
                                               BundleStatusReport.DELETED) ? true : false);
                
                // TODO: implement reasonCode
                StatusReport statusReport = new StatusReport(received, forwarded, delivered, deleted,
                            0, statusReportRevd.getSourceNodeID(), statusReportRevd.getBundleTimestamp());
    
                if (status == BundleStatusReport.DELETED) {
                    // package's status is DELETE, resend with current time-stemp
                    // TODO: send resend flag to BPA (Verify)
                    BPA.getInstance().resendBundle(statusReportRevd.getBundleTimestamp());
                }
                else {
                    // TODO: Verify it
                    // TODO: What to return for nodeID. Previous Node's ID or Src's NodeID, or current Node's NodeID.
                    // TODO: Ask if getSourceNodeID() returns the actual src's NodeID.
                    byte[] b_statusReport = AdminElement.objectToByteArray(statusReport);
                    // BPA.getInstance().sendWithAdminFlag(b_statusReport, statusReportRevd.getSourceNodeID()); 
                    BPA.getInstance().sendWithACK(b_statusReport, statusReportRevd.getSourceNodeID());
                }
    
    
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                // TODO: How to handle the interruptedException

                Thread.currentThread().interrupt(); // Stop the thread
                break;
            } catch (IOException e) {
                // TODO Auto-generated catch block for b_statusReport
                // If the byte array cannot be generated from the statusReport object, run this
                // e.printStackTrace();
                continue;
            }
        }
    }
}
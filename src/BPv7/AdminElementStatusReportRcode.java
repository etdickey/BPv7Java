package BPv7;
import BPv7.containers.StatusReport;
import BPv7.containers.Timestamp;
import BPv7.utils.BundleStatusReport;
import BPv7.utils.StatusReportUtilObject;
import Configs.SimulationParams;

import java.util.logging.Logger;

import static BPv7.BPA.*;

public class AdminElementStatusReportRcode implements Runnable {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(AdminElementStatusReportRcode.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static AdminElementStatusReportRcode instance = null;
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
    public static AdminElementStatusReportRcode getInstance() {
        if(instance == null){
            synchronized (AdminElementStatusReportRcode.class) {
                if(instance == null) {
                    instance = new AdminElementStatusReportRcode();
                    logger.info("Created AdminElementStatusReportRcode singleton");
                }
            }
        }
        return instance;
    }

    protected AdminElementStatusReportRcode() {}

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
                // redundant code, take will wait till it gets data
                if (BPA.readStatusReportBuffer.isEmpty()) {
                    continue; // Skip the following code
                }

                byte[] payload = BPA.readStatusReportBuffer.take();
                StatusReport replyStatusReport = (StatusReport)AdminElement.bytesToObject(payload);
                int reasonCode = replyStatusReport.getReasonCode();
                this.action(reasonCode, replyStatusReport.getCreationTimestamp());

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                continue;
            }
        }
    }

    /*
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
     */
    private void action(int reasonCode, Timestamp timestamp) {
        if (reasonCode == 1) {
            // Lifetime expired.
            if(SimulationParams.lifetimeExpiredAction) {
                // resend!
                BPA.getInstance().resendBundle(timestamp);
            } //else {
                // drop
                // this statusreport was popped already.
                // So doing nothing is going to be the same as dropping.
            // }
        }
        else if (reasonCode == 2) {
            // Forwarded over unidirectional link.
            /*
            Do not expect to receive ACK or anything else
            So nothing I guess?
             */
            if(SimulationParams.overUnidirectionalAction){
                // nothing?
            } else {
                // nothing?
            }
        }
        else if (reasonCode == 3) {
            // Transmission canceled.
            if(SimulationParams.transmissionCancelledAction) {
                BPA.getInstance().resendBundle(timestamp);
            }
        }
        else if (reasonCode == 4) {
            // Depleted storage.
            if(SimulationParams.depletedStorageAction) {
                BPA.getInstance().resendBundle(timestamp);
            }
        }
        else if (reasonCode == 5) {
            // Destination endpoint ID unavailable.
            if(SimulationParams.destinationUnavailableAction) {
                BPA.getInstance().resendBundle(timestamp);
            }
        }
        else if (reasonCode == 6) {
            // No known route to destination from here.
            if(SimulationParams.noKnownRouteToDestinationAction) {
                BPA.getInstance().resendBundle(timestamp);
            }
        }
        else if (reasonCode == 7) {
            // No timely contact with next node on route.
            if(SimulationParams.noTimelyContactAction) {
                BPA.getInstance().resendBundle(timestamp);
            }
        }
        else if (reasonCode == 8) {
            // Block unintelligible.
            if(SimulationParams.blockUnintelligibleAction) {
                BPA.getInstance().resendBundle(timestamp);
            }
        }
        else if (reasonCode == 9) {
            // Hop limit exceeded.
            if(SimulationParams.hopLimitExceededAction) {
                BPA.getInstance().resendBundle(timestamp);
            }
        }
        else if (reasonCode == 10) {
            // Traffic pared (e.g., status reports).
            if(SimulationParams.trafficParedAction) {
                BPA.getInstance().resendBundle(timestamp);
            }
        }
        else if (reasonCode == 11) {
            // Block unsupported.
            if(SimulationParams.blockUnsupportedAction) {
                BPA.getInstance().resendBundle(timestamp);
            }
        }
        else {
            // 17-254 -> Unassigned.
            // 255 -> Reserved.
        }
    }
}
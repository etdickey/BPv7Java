package BPv7;

import BPv7.containers.*;
import BPv7.utils.BundleDispatchStatusMap;
import BPv7.utils.BundleStatusReport;
import DTCP.DTCP;
import DTCP.interfaces.DTCPInterface;
import DTCP.interfaces.ReachableStatus;

import java.util.InvalidPropertiesFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static BPv7.BPA.bundleStatusMap;
import static BPv7.BPA.sendBuffer;
import static BPv7.utils.DispatchStatus.DELETED;
import static BPv7.utils.DispatchStatus.SENT;

/**
 * BPA Dispatcher class is used to send bundles from BPA to DTCP
 */
public class BPADispatcher implements Runnable {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(BPADispatcher.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     *
     * @implNote not making this volatile because its value only changes once
     * (null -> instance), thus only one set of double-checked locking is needed
     * (caching ok because all variables are final)
     */
    private static BPADispatcher instance = null;
    /**
     * DTCP class Instance
     */
    private static final DTCPInterface dtcp = DTCP.getInstance();
    /**
     * BPA Util class Instance
     */
    private static final BPAUtils bpaUtils = BPAUtils.getInstance();


    //functions!

    /**
     * Gets the singleton instance of the BPADispatcher
     * @return a reference to the simulation parameters instance
     * @implNote not making this instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static BPADispatcher getInstance() {
        //noinspection DoubleCheckedLocking
        if (instance == null) {
            synchronized (BPADispatcher.class) {
                if (instance == null) {
                    instance = new BPADispatcher();
                    logger.info("Created BPADispatcher singleton");
                }
            }
        }
        return instance;
    }

    /**
     * Hiding this constructor to force use of singleton accessor getInstance()
     */
    protected BPADispatcher() {}

    /**
     * passive sender thread: will spawn when we want to send a message and only remain alive until all messages are sent.
     * More efficient than having it busy-wait until a new message is ready to be sent. Checks if destination node ID
     * can be reached and tries to send it. If sending fails, deleting is attempted. If deleted, bundleStatusMap is updated,
     * otherwise the bundle is re-added to the sendBuffer.
     */
    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while(true) {
            Bundle bundleToSend;
            try {
                bundleToSend = sendBuffer.take();
            } catch (InterruptedException e) {
                logger.severe("Unable to get bundle from the Queue (sendBuffer). " +
                        "Queue was interrupted: " + e.getMessage());
                continue;
            }
            NodeID destNode = bundleToSend.getPrimary().getDestNode();
            Timestamp creationTimestamp = bundleToSend.getPrimary().getCreationTimestamp();
            boolean deliverFlag = bundleToSend.getPrimary().getDLIV();
            ReachableStatus reachable = dtcp.canReach(destNode);
            if(reachable == ReachableStatus.REACHABLE) {
                logger.info("DTCP reported: Can reach destination nodeID");
                if(dtcp.send(bundleToSend)) {//try to send
                    bundleStatusMap.put(creationTimestamp, new BundleDispatchStatusMap(bundleToSend, SENT));
                    logger.info("[NetStats] BPA Sent: " + bundleToSend.getLoggingBundleId()
                                        + "; Time (ms) since creation: " + (DTNTime.getCurrentDTNTime().timeInMS - bundleToSend.getPrimary().getCreationTimestamp().creationTime().timeInMS));
                    logger.info("Sent bundle to DTCP and updated the dispatch status for bundle timestamp: " +
                            bundleToSend.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
                } else {//failed to send
                    if(!canDelete(bundleToSend)) {//try to delete
                        sendBuffer.add(bundleToSend);
                        logger.info("Adding the bundle again to the queue for resending, timestamp: " +
                                bundleToSend.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
                    } else {//delete!
                        // send status report if ack requested
                        if (deliverFlag) {
                            StatusReport statusReport = bpaUtils.sendStatusReport(bundleToSend, BundleStatusReport.DELETED, 1);
                            Bundle statusReportBundle;
                            try {
                                statusReportBundle = bpaUtils.createBundle(BPAUtils.objectToByteArray(statusReport), bundleToSend.getPrimary().getDestNode(), true, false);
                                sendBuffer.add(statusReportBundle);
                                logger.info("Sending status report, timestamp: " +
                                        bundleToSend.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
                            } catch (InvalidPropertiesFormatException e) {
                                logger.severe("Unable to parse status report! " + e.getMessage());
                                //drop status report
                            }
                        }
                        bundleStatusMap.put(creationTimestamp, new BundleDispatchStatusMap(bundleToSend, DELETED));
                        logger.info("[NetStats] Bundle Deleted: " + bundleToSend.getLoggingBundleId()
                                            + "; Time (ms) since creation: " + (DTNTime.getCurrentDTNTime().timeInMS - bundleToSend.getPrimary().getCreationTimestamp().creationTime().timeInMS)
                                            + "; Size of bundle payload (bytes):" + bundleToSend.getPayload().getPayload().length);
                        logger.info("deleted the bundle, timestamp: " +
                                bundleToSend.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
                    }
                }
            } else {//can't reach destination for some reason...
                switch (reachable) {
                    case EXPECTED_DOWN ->  {
                        if (bundleToSend.getPrimary().getLifetime() + bundleToSend.getPrimary().getCreationTimestamp().creationTime().timeInMS <= DTNTime.getCurrentDTNTime().timeInMS) {
                            logger.log(Level.INFO, "Bundle not deliverable due to expected down and reached lifetime. Bundle: " + bundleToSend.getLoggingBundleId());
                        } else {
                            // This means it's temporarily down, but might not be in the future and hasn't reached lifetime, so re add it to the (back of) the queue
                            //noinspection ResultOfMethodCallIgnored
                            sendBuffer.offer(bundleToSend);
                            continue;
                        }
                    }
                    case NO_ROUTE -> logger.log(Level.INFO, "Bundle not deliverable due to no known route. Bundle: "
                            + bundleToSend.getLoggingBundleId());
                    case UNKNOWN_ID -> logger.log(Level.INFO, "Bundle not deliverable due to an unknown Destination ID = \""
                            + bundleToSend.getPrimary().getDestNode().id() + "\". Bundle: " + bundleToSend.getLoggingBundleId());
                }
                // send status report if ack requested
                if (deliverFlag) {
                    StatusReport statusReport = bpaUtils.sendStatusReport(bundleToSend, BundleStatusReport.DELETED, 5);
                    Bundle statusReportBundle;
                    try {
                        statusReportBundle = bpaUtils.createBundle(BPAUtils.objectToByteArray(statusReport), bundleToSend.getPrimary().getDestNode(), true, false);
                        sendBuffer.add(statusReportBundle);
                        logger.info("deleted the bundle. Sending status report, timestamp: " +
                                bundleToSend.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
                    } catch (InvalidPropertiesFormatException e) {
                        logger.severe("Unable to parse status report! " + e.getMessage());
                        //drop status report
                    }
                }

            }
        }
    }

    /**
     * Checks if bundle can be deleted based on LifeTime set in Primary block
     * @param bundle: bundle to be checked
     * @return boolean(), true if we can delete it, else false.
     */
    private boolean canDelete(Bundle bundle) {
        long timeGap = Math.subtractExact(DTNTime.getCurrentDTNTime().getTimeInMS(), bundle.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
        return timeGap > bundle.getPrimary().getLifetime();
    }
}

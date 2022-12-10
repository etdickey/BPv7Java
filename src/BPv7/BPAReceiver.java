package BPv7;

import BPv7.containers.*;
import BPv7.utils.BundleStatusReport;
import BPv7.utils.StatusReportUtilObject;
import Configs.SimulationParams;
import DTCP.DTCP;
import DTCP.interfaces.DTCPInterface;

import java.util.InvalidPropertiesFormatException;
import java.util.logging.Logger;

import static BPv7.BPA.*;
import static BPv7.utils.BundleStatusReport.DELIVERED;
import static BPv7.utils.BundleStatusReport.FORWARDED;

/**
 * BPA class for receiving bundles form DTCP
 */
public class BPAReceiver implements Runnable {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(BPAReceiver.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     *
     * @implNote not making this volatile because its value only changes once
     * (null -> instance), thus only one set of double-checked locking is needed
     * (caching ok because all variables are final)
     */
    private static BPAReceiver instance = null;
    /**
     * dtcp class Instance
     */
    private static final DTCPInterface dtcp = DTCP.getInstance();
    /**
     * Simulate params class Instance
     */
    private static final SimulationParams simulationParams = SimulationParams.getInstance();
//    /**
//     * BPA class Instance
//     */
//    private static final BPA bpa = BPA.getInstance();
    /**
     * BPA util class Instance
     */
    private static final BPAUtils bpaUtils = BPAUtils.getInstance();


    //functions!

    /**
     * Gets the singleton instance of the BPADispatcher
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static BPAReceiver getInstance() {
        //noinspection DoubleCheckedLocking
        if(instance == null){
            synchronized (BPAReceiver.class) {
                if(instance == null) {
                    instance = new BPAReceiver();
                    logger.info("Created BPAReceiver singleton");
                }
            }
        }
        return instance;
    }

    /**
     * Hiding this constructor to force use of singleton accessor getInstance()
     */
    protected BPAReceiver() {}

    /**
     * Receives bundles and checks if the bundle is to be deleted. If it is, creates a status report bundle and sends it back to the sender.
     * If not, checks if destination node ID is itself. If it is not, checks acknowledgment requirement and either adds acknowledgment
     * bundle and saves bundle to queue, or just saves bundle to queue. If destination node id is itself, adds to status
     * report buffer if it's an admin bundle, otherwise adds to receive buffer and also generated acknowledgment if needed.
     */
    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while(true) {
            Bundle bundle = dtcp.recv();


            int deletionCode = bpaUtils.checkIfBundleToDelete(bundle);
            boolean deliveryFlag = bundle.getPrimary().getDLIV();
            boolean adminFlag = bundle.getPrimary().isAdminRecord();
            //if we are deleting it and an delivery ACK is requested (and not admin record), send a status report back to the sender 
            if (deletionCode != -1 && deliveryFlag && !adminFlag) {
                logger.info("Deleting bundle reason code = " + deletionCode + "! " + bundle.getLoggingBundleId());
                if(deletionCode == 1){
                    long timeGap = Math.subtractExact(DTNTime.getCurrentDTNTime().getTimeInMS(), bundle.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
                    logger.info("  deleting bundle because it expired, bundle = " + bundle.getLoggingBundleId()
                            + "; TTL = " + bundle.getPrimary().getLifetime() + "; time since creation: " + timeGap);
                }
                StatusReport statusReport = bpaUtils.sendStatusReport(bundle, BundleStatusReport.DELETED, deletionCode);
                Bundle statusReportBundle;
                try {
                    statusReportBundle = bpaUtils.createBundle(BPAUtils.objectToByteArray(statusReport), bundle.getPrimary().getSrcNode(), true, false);
                    sendBuffer.add(statusReportBundle);
                    logger.info("Sending status report for deleted bundle, timestamp: " +
                            statusReportBundle.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
                } catch (InvalidPropertiesFormatException e) {
                    logger.severe("Unable to parse status report! " + e.getMessage());
                    //drop status report
                }
                logger.info("[NetStats] Bundle Deleted: " + bundle.getLoggingBundleId()
                        + "; Time (ms) since creation: " + (DTNTime.getCurrentDTNTime().timeInMS - bundle.getPrimary().getCreationTimestamp().creationTime().timeInMS)
                        + "; Size of bundle payload (bytes):" + bundle.getPayload().getPayload().length);
                continue;
            }


            logger.info("[NetStats] BPA Received: " + bundle.getLoggingBundleId()
                    + "; Time (ms) since creation: " + (DTNTime.getCurrentDTNTime().timeInMS - bundle.getPrimary().getCreationTimestamp().creationTime().timeInMS));



            // read nodeID of system and see if matches bundle destination ID
            if (bundle.getPrimary().getDestNode().id().equals(simulationParams.hostID)) {//REACHED DESTINATION (this node)
                // if bundle has admin flag set
                // add the payload to readStatusReportBuffer
                if (adminFlag) {
                    readStatusReportBuffer.add(bundle.getPayload().getPayload());
                    logger.info("Added status report to the queue for AA");
                } else {
                    // else if bundle has ack flag set
                    // make a StatusReportUtilObject object add to sendStatusReportBuffer
                    if (deliveryFlag) {
                        NodeID nodeID = bundle.getPrimary().getSrcNode();
                        Timestamp timestamp = bundle.getPrimary().getCreationTimestamp();
                        sendStatusReportBuffer.add(new StatusReportUtilObject(nodeID, timestamp, DELIVERED));
                        logger.info("Sending status report for delivered bundle, timestamp: " +
                                timestamp.creationTime().getTimeInMS());
                    }
                    // add bundle to receiveBuffer
                    receiveBuffer.add(bundle);
                    logger.info("Added bundle to the queue for AA, timestamp: " +
                            bundle.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
                }
                logger.info("[NetStats] Bundle Arrived: " + bundle.getLoggingBundleId()
                        + "; Time (ms) since creation: " + (DTNTime.getCurrentDTNTime().timeInMS - bundle.getPrimary().getCreationTimestamp().creationTime().timeInMS)
                        + "; Size of bundle payload (bytes):" + bundle.getPayload().getPayload().length);
            } else {//FORWARD
                // check if bundle has ack flag
                if (deliveryFlag && !adminFlag) {
                    NodeID nodeID = bundle.getPrimary().getSrcNode();
                    Timestamp timestamp = bundle.getPrimary().getCreationTimestamp();
                    sendStatusReportBuffer.add(new StatusReportUtilObject(nodeID, timestamp, FORWARDED));
                    logger.info("Sending status report for forwarded bundle, timestamp: " +
                            timestamp.creationTime().getTimeInMS());
                }
                bpaUtils.saveToQueue(bundle);
                logger.info("Added bundle to send to DTCP, timestamp: " +
                        bundle.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
            }
        }
    }
}

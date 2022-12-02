package BPv7;

import BPv7.containers.*;
import BPv7.utils.BundleStatusReport;
import BPv7.utils.StatusReportUtilObject;
import Configs.SimulationParams;
import DTCP.DTCP;
import DTCP.interfaces.DTCPInterface;
import jdk.jshell.Snippet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import static BPv7.BPA.*;
import static BPv7.utils.BundleStatusReport.DELIVERED;
import static BPv7.utils.BundleStatusReport.FORWARDED;

public class BPAReceiver implements Runnable {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(BPAReceiver.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static BPAReceiver instance = null;
    /**
     * dtcp and SimulationParans Instance for using functions
     */
    private final DTCPInterface dtcp = DTCP.getInstance();
    private final SimulationParams simulationParams = SimulationParams.getInstance();


    //functions!

    /**
     * Gets the singleton instance of the BPADispatcher
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static BPAReceiver getInstance() {
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
     * Convert an object to a bytes array
     */
    public static byte[] objectToByteArray(StatusReport obj)  {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            return yourBytes;
        }catch (Exception e) {
            //Add a logger
            return null;
        }finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }
    /**
     * @param bundle: bundle to be checked for deletion
     * @return : reason code if bundle needs to be deleted, -1 otherwise
     */
    private int checkIfBundleToDelete(Bundle bundle) {
        long timeGap = Math.subtractExact(System.currentTimeMillis(), bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS());
        if(timeGap > bundle.getPrimary().getLifetime()) {
            return 1;
        }else if(!dtcp.canReach(bundle.getPrimary().getDestNode())) {
            return 5;
        }else if(bundle.getPayload() == null || bundle.getPayload().getPayload() == null) {
            return 11;
        }else {
            return -1;
        }
    }
    /**
     * @param bundle: bundle to create status report for
     * @param status : the state of different status's for the bundle, ie deleted, forwarded, delivered, received
     * @param rCode : The reason code
     * @return : reason code if bundle needs to be deleted, -1 otherwise
     */
    public StatusReport sendStatusReport(Bundle bundle, BundleStatusReport status, int rCode) {
        Timestamp timestamp = bundle.getPrimary().getCreationTimestamp();
        NodeID destNode = bundle.getPrimary().getDestNode();
        BundleStatusItem received = new BundleStatusItem(true);
        BundleStatusItem forwarded = new BundleStatusItem((status ==
                BundleStatusReport.FORWARDED) ? true : false);
        BundleStatusItem delivered = new BundleStatusItem((status ==
                BundleStatusReport.DELIVERED) ? true : false);
        BundleStatusItem deleted = new BundleStatusItem((status ==
                BundleStatusReport.DELETED) ? true : false);

        StatusReport rep = new StatusReport(received,forwarded,delivered,deleted,rCode,destNode,timestamp);
        return rep; //send the status report
    }

    protected BPAReceiver() {}

    /**
     * Receives bundles and checks if the bundle is to be deleted. If it is, creates a status report and bundle. If not,
     * checks if destination node ID is 1. If it is not, checks acknowledgment requirement and either adds acknowledgment
     * bundle and saves bundle to queue, or just saves bundle to queue. If destination node id was 1, adds to status
     * report buffer if it's an admin bundle, otherwise adds to receive buffer and also generated acknowledgment if needed.
     */
    @Override
    public void run() {
        while(true) {
            Bundle bundle = dtcp.recv();
            // TODO: @ethan get id of current NodeID
            // read nodeID of system and see if matches bundle destination ID
            // TODO: change if condition
            int deletionCode = checkIfBundleToDelete(bundle);
            boolean ackFlag = (bundle.getPrimary().getFlags() & 0x20) == 1;
            if(deletionCode != -1 && ackFlag) {
                StatusReport statusReport = sendStatusReport(bundle, BundleStatusReport.DELETED, deletionCode);
                BPA.getInstance().createBundle(objectToByteArray(statusReport), bundle.getPrimary().getDestNode(), true, false);
                continue;
            }

            if(bundle.getPrimary().getDestNode().id() == "1") {


                // if bundle has admin flag set
                // add the payload to readStatusReportBuffer
                if ((bundle.getPrimary().getFlags() & 0x02) == 1) {
                    readStatusReportBuffer.add(bundle.getPayload().getPayload());
                } else {
                    // else bundle has ack flag set
                    // make a StatusReportUtilObject object add to sendStatusReportBuffer
                    if(ackFlag) {
                        NodeID nodeID = bundle.getPrimary().getDestNode();
                        Timestamp timestamp = bundle.getPrimary().getCreationTimestamp();
                        sendStatusReportBuffer.add(new StatusReportUtilObject(nodeID, timestamp, DELIVERED));
                    }
                    // add bundle to receiveBuffer
                    receiveBuffer.add(bundle);
                }
            } else {
                // check if bundle has ack flag
                if(ackFlag) {
                    NodeID nodeID = bundle.getPrimary().getDestNode();
                    Timestamp timestamp = bundle.getPrimary().getCreationTimestamp();
                    sendStatusReportBuffer.add(new StatusReportUtilObject(nodeID, timestamp, FORWARDED));
                }
                BPA.getInstance().saveToQueue(bundle);
            }
        }
    }
}

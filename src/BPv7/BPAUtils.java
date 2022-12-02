package BPv7;

import BPv7.containers.*;
import BPv7.utils.BundleDispatchStatusMap;
import BPv7.utils.BundleStatusReport;
import BPv7.utils.DispatchStatus;
import DTCP.DTCP;
import DTCP.interfaces.DTCPInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import static BPv7.utils.DispatchStatus.NONE;
import static BPv7.utils.DispatchStatus.PENDING;

public class BPAUtils {
    /**
     * Logger for this class. Prepends all logs from this class with the class name
     */
    private static final Logger logger = Logger.getLogger(BPAUtils.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     *
     * @implNote not making this volatile because its value only changes once
     * (null -> instance), thus only one set of double-checked locking is needed
     * (caching ok because all variables are final)
     */
    private static BPAUtils instance = null;
    private static BPA bpa = BPA.getInstance();
    private static DTCPInterface dtcp = DTCP.getInstance();

    //functions!

    /**
     * Hiding this constructor to force use of singleton accessor getInstance()
     */
    protected BPAUtils() {
    }

    /**
     * Gets the singleton instance of the BPA
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this instance volatile because its value only changes once
     * (null -> instance), thus only one set of double-checked locking is needed
     */
    public static BPAUtils getInstance() {
        if (instance == null) {
            synchronized (BPAUtils.class) {
                if (instance == null) {
                    instance = new BPAUtils();
                    logger.info("Created BPA singleton");
                }
            }
        }
        return instance;
    }

    /**
     * Convert an object to a bytes array
     */
    public static byte[] objectToByteArray(StatusReport obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            return yourBytes;
        } catch (Exception e) {
            //Add a logger
            return null;
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    /**
     * Util function to check status of bundle
     *
     * @param key: bundle key
     * @return status of the bundle, NONE if invalid key
     */
    public DispatchStatus getBundleStatus(Timestamp key) {
        if (key.getSeqNum() != -1 && bpa.bundleStatusMap.containsKey(key)) {
            logger.info("Bundle status for timestamp " + key.getCreationTime().getTimeInMS());
            return bpa.bundleStatusMap.get(key).getStatus();
        }
        logger.warning("Unable to get bundle status");
        return NONE;
    }

    /**
     * Util function to save bundle to queue
     *
     * @param bundle: bundle to be saved
     * @return creation Timestamp of the bundle
     */
    public Timestamp saveToQueue(Bundle bundle) {
        bpa.sendBuffer.add(bundle);
        Timestamp creationTimestamp = bundle.getPrimary().getCreationTimestamp();
        bpa.bundleStatusMap.put(creationTimestamp, new BundleDispatchStatusMap(bundle, PENDING));
        logger.info("save the bundle in the sending queue: " + bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS());
        return creationTimestamp;
    }

    /**
     * Util function to check status of bundle
     *
     * @param payload:  bundle payload
     * @param destID    : destination node id for the bundle
     * @param adminFlag : true if status report, false if normal bundle
     * @param ackFlag   : true if acknowledgement required, false otherwise
     * @return newly created bundle
     */
    public Bundle createBundle(byte[] payload, NodeID destID, boolean adminFlag, boolean ackFlag) {
        Bundle bundle = new Bundle();
        // TODO: @ethan read lifetime from config files
        PrimaryBlock primaryBlock = new PrimaryBlock(destID, NodeID.getNullSourceID(), 500);
        if (adminFlag) {
            primaryBlock.setADMN();
        }
        if (ackFlag) {
            primaryBlock.setACKR();
        }
        PayloadBlock payloadBlock = new PayloadBlock(payload);
        bundle.setPrimary(primaryBlock);
        bundle.setPayload(payloadBlock);
        logger.info("creating bundle for the payload");
        return bundle;
    }

    /**
     * @param bundle: bundle to be checked for deletion
     * @return : reason code if bundle needs to be deleted, -1 otherwise
     */
    public int checkIfBundleToDelete(Bundle bundle) {
        long timeGap = Math.subtractExact(System.currentTimeMillis(), bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS());
        if (timeGap > bundle.getPrimary().getLifetime()) {
            return 1;
        } else if (!dtcp.canReach(bundle.getPrimary().getDestNode())) {
            return 5;
        } else if (bundle.getPayload() == null || bundle.getPayload().getPayload() == null) {
            return 11;
        } else {
            return -1;
        }
    }

    /**
     * @param bundle: bundle to create status report for
     * @param status  : the state of different status's for the bundle, ie deleted, forwarded, delivered, received
     * @param rCode   : The reason code
     * @return : reason code if bundle needs to be deleted, -1 otherwise
     */
    public StatusReport sendStatusReport(Bundle bundle, BundleStatusReport status, int rCode) {
        Timestamp timestamp = bundle.getPrimary().getCreationTimestamp();
        NodeID destNode = bundle.getPrimary().getDestNode();
        BundleStatusItem received = new BundleStatusItem(true);
        BundleStatusItem forwarded = new BundleStatusItem(status == BundleStatusReport.FORWARDED);
        BundleStatusItem delivered = new BundleStatusItem(status == BundleStatusReport.DELIVERED);
        BundleStatusItem deleted = new BundleStatusItem(status == BundleStatusReport.DELETED);

        StatusReport rep = new StatusReport(received, forwarded, delivered, deleted, rCode, destNode, timestamp);
        //send the status report
        return rep;
    }
}

package BPv7;

import BPv7.containers.*;
import BPv7.utils.BundleDispatchStatusMap;
import BPv7.utils.BundleStatusReport;
import BPv7.utils.DispatchStatus;
import Configs.SimulationParams;
import DTCP.DTCP;
import DTCP.interfaces.DTCPInterface;
import DTCP.interfaces.ReachableStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.logging.Logger;

import static BPv7.utils.DispatchStatus.NONE;
import static BPv7.utils.DispatchStatus.PENDING;

/**
 * BPA Util class for all utility functions
 */
public class BPAUtils {
    /**
     * Logger for this class. Prepends all logs from this class with the class name
     */
    private static final Logger logger = Logger.getLogger(BPAUtils.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     * (null -> instance), thus only one set of double-checked locking is needed
     * (caching ok because all variables are final)
     */
    private static BPAUtils instance = null;
//    /**
//     * BPA class instance
//     */
//    private static final BPA bpa = BPA.getInstance();
    /**
     * DTCP class instance
     */
    private static final DTCPInterface dtcp = DTCP.getInstance();

    //functions!

    /**
     * Hiding this constructor to force use of singleton accessor getInstance()
     */
    protected BPAUtils() {}

    /**
     * Gets the singleton instance of the BPA
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this instance volatile because its value only changes once
     * (null -> instance), thus only one set of double-checked locking is needed
     */
    public static BPAUtils getInstance() {
        //noinspection DoubleCheckedLocking
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
     * Function to convert object to byte array (serialization)
     * @param obj status report object
     * @return byte array
     * @throws InvalidPropertiesFormatException if bad status report
     */
    public static byte[] objectToByteArray(StatusReport obj) throws InvalidPropertiesFormatException{//if CBOR is desired:: call CBOR functions here
        ObjectMapper mapper = new ObjectMapper();


        try {
            //            logger.info("Wrote this bundle as JSON:\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj));
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            logger.severe("ERROR! Unable to write status report to byte[]: " + e.getMessage());
            throw new InvalidPropertiesFormatException(e.getMessage());
        }

//        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
//            ObjectOutputStream out;
//            out = new ObjectOutputStream(bos);
//            out.writeObject(obj);
//            out.flush();
//            return bos.toByteArray();
//        } catch (Exception e) {
//            logger.severe("Unable to convert object to byte array");
//            return null;
//        }
        // ignore close exception
    }

    /**
     * Util function to check status of bundle
     * @param key: bundle key
     * @return status of the bundle, NONE if invalid key
     */
    @SuppressWarnings("unused")
    public DispatchStatus getBundleStatus(Timestamp key) {
        if (key.seqNum() != -1 && BPA.bundleStatusMap.containsKey(key)) {
            DispatchStatus status = BPA.bundleStatusMap.get(key).status();
            logger.info("Bundle status" + status + " for timestamp " + key.creationTime().getTimeInMS());
            return status;
        }
        logger.warning("Unable to get bundle status for bundle timestamp: " + key);
        return NONE;
    }

    /**
     * Util function to save bundle to sending queue (sendBuffer)
     * @param bundle: bundle to be saved
     * @return creation Timestamp of the bundle
     */
    public Timestamp saveToQueue(Bundle bundle) {
        BPA.sendBuffer.add(bundle);
        Timestamp creationTimestamp = bundle.getPrimary().getCreationTimestamp();
        BPA.bundleStatusMap.put(creationTimestamp, new BundleDispatchStatusMap(bundle, PENDING));
        logger.info("save the bundle in the sending queue: " + bundle.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
        return creationTimestamp;
    }

    /**
     * Util function to check status of bundle
     * @param payload:  bundle payload
     * @param destID    : destination node id for the bundle
     * @param adminFlag : true if status report, false if normal bundle
     * @param ackFlag   : true if acknowledgement required, false otherwise
     * @return newly created bundle
     */
    public Bundle createBundle(byte[] payload, NodeID destID, boolean adminFlag, boolean ackFlag) {
        //read lifetime from config files
        PrimaryBlock primaryBlock = new PrimaryBlock(destID, new NodeID(SimulationParams.getInstance().hostID),
                                                     SimulationParams.getInstance().scenario.bundleLifetimeMS());
        if (adminFlag) {
            primaryBlock.setADMN();
        }
        if (ackFlag) {
            primaryBlock.setDLIV();
            primaryBlock.setFRWD();
            primaryBlock.setDELT();
        }

        PayloadBlock payloadBlock = new PayloadBlock(payload);
        Bundle bundle = new Bundle(primaryBlock, payloadBlock, null);

        logger.info("creating bundle for the payload, bundle = " + bundle.getLoggingBundleId());
        return bundle;
    }

    /**
     * @param bundle: bundle to be checked for deletion
     * @return : reason code if bundle needs to be deleted, -1 otherwise
     */
    public int checkIfBundleToDelete(Bundle bundle) {
        long timeGap = Math.subtractExact(DTNTime.getCurrentDTNTime().getTimeInMS(), bundle.getPrimary().getCreationTimestamp().creationTime().getTimeInMS());
        if (timeGap > bundle.getPrimary().getLifetime()) {
            return 1;
        } else if (dtcp.canReach(bundle.getPrimary().getDestNode()) == ReachableStatus.UNKNOWN_ID) {
            return 5;
        } else if (dtcp.canReach(bundle.getPrimary().getDestNode()) == ReachableStatus.NO_ROUTE) {
            return 6;
        }  else if (dtcp.canReach(bundle.getPrimary().getDestNode()) == ReachableStatus.EXPECTED_DOWN) {
            return 7;
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
        logger.info("Created status report for sending: " + rep.getLoggingId());
        //send the status report
        return rep;
    }
}

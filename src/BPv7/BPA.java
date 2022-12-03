package BPv7;


import BPv7.containers.Bundle;
import BPv7.containers.DTNTime;
import BPv7.containers.NodeID;
import BPv7.containers.Timestamp;
import BPv7.interfaces.BPAInterface;
import BPv7.utils.BundleDispatchStatusMap;
import BPv7.utils.StatusReportUtilObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

/**
 * Main class responsible for handling BP protocol and handling of bundles.
 * todo:: check {@link BPv7.interfaces.BPAInterface} for more comments on implementation requirements
 */
class BPA implements BPAInterface {//package-private (not private/public)
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(BPA.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     *
     * @implNote not making this volatile because its value only changes once
     * (null -> instance), thus only one set of double-checked locking is needed
     * (caching ok because all variables are final)
     */
    private static BPA instance = null;

    //actual class variables
    /**
     * BPA Util class instance
     */
    private static final BPAUtils bpaUtils = null;
    /**
     * Queue for creating status reports for bundles
     */
    public static BlockingQueue<StatusReportUtilObject> sendStatusReportBuffer = new LinkedBlockingDeque<>();
    /**
     * Queue of Bundles to be sent
     */
    protected static BlockingQueue<Bundle> sendBuffer = new LinkedBlockingDeque<>();
    /**
     * Queue of received bundles
     */
    protected static BlockingQueue<Bundle> receiveBuffer = new LinkedBlockingDeque<>();
    /**
     * Queue for reading status reports from bundles
     */
    public static BlockingQueue<byte[]> readStatusReportBuffer = new LinkedBlockingDeque<>();
    /**
     * Threads for sending and receiving
     */
    private final Thread sendingThread;
    private final Thread receivingThread;
    /**
     * Map to maintain bundle dispatching status
     */
    public static Map<Timestamp, BundleDispatchStatusMap> bundleStatusMap = new HashMap<>();


    //functions!

    /**
     * Gets the singleton instance of the BPA
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this instance volatile because its value only changes once
     * (null -> instance), thus only one set of double-checked locking is needed
     */
    public static BPA getInstance(){
        if(instance == null){
            synchronized (BPA.class){
                if(instance == null){
                    instance = new BPA();
                    logger.info("Created BPA singleton");
                }
            }
        }
        return instance;
    }

    /**
     * Hiding this constructor to force use of singleton accessor getInstance()
     * Start sending and receiving threads for bundles
     */
    protected BPA() {
        sendingThread = new Thread(new BPADispatcher());
        sendingThread.start();
        receivingThread = new Thread(new BPAReceiver());
        receivingThread.start();
        logger.info("Started BPA sending and receiving threads");
    }

    //@ethan todo:: switch branches/close containers.
//    /**
//     * Blocking call that waits for the sending buffer to empty (or only contain unreachable objects)
//     * @return true if the queue only contains unreachable objects, or false if it contains no objects
//     */
//    public boolean waitUntilAllMessagesAreSentOrUnreachable(){
//        synchronized(sendBuffer){
//            while(!sendBuffer.isEmpty()){
//                //todo:: Ethan:: check if only contains unreachable objects
//
//                //if not, wait until stuff gets sent
//                sendBuffer.wait();//release lock and wait to be notified that it is empty by sending thread
//            }
//            //once here, done, unblock caller
//        }
//    }

    /**
     * Gets the payload of the next admin bundle (which is just an admin record).
     *
     * @return the payload of the next admin bundle
     */
    @Override
    public byte[] getAdminRecord() {
        try {
            logger.info("getting admin record from queue");
            return readStatusReportBuffer.take();
        } catch (InterruptedException e) {
            // log error
            logger.severe("Unable to get admin records from the Queue (readStatusReportBuffer). " +
                    "Queue was interrupted: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns the next bundle’s entire payload
     * @return byteStream of payload
     */
    public byte[] getPayload() {
        Bundle bundle = null;
        try {
            bundle = receiveBuffer.take();
            if (bundle.getPayload() != null && bundle.getPayload().getPayload() != null) {
                logger.info("sending payload of the bundle to AA, timestamp: " +
                        bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS());
                return bundle.getPayload().getPayload();
            }
        } catch (InterruptedException e) {
            // log error
            logger.severe("Unable to get bundle payload from the Queue (receiveBuffer). " +
                    "Queue was interrupted: " + e.getMessage());
            return null;
        }
        return null;
    }

    /**
     * create and save the bundle to outgoing queue
     * @param payload Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return key (timestamp) for the bundle else invalid timestamp
     */
    public Timestamp send(byte[] payload, NodeID destNodeID) {
        if(payload != null && payload.length > 0) {
            Bundle bundle = bpaUtils.createBundle(payload, destNodeID, false, false);
            logger.info("saving the bundle, timestamp: " + bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS());
            // save to queue
            return bpaUtils.saveToQueue(bundle);
        }
        logger.warning("Unable to save the bundle to the queue");
        return new Timestamp(DTNTime.getUnknownDTNTime(), -1);
    }

    /**
     * create the bundle and save to sending queue with ACK flag true
     * @param payload Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return key (timestamp) for the bundle else invalid timestamp
     */
    @Override
    public Timestamp sendWithACK(byte[] payload, NodeID destNodeID) {
        if(payload != null && payload.length > 0) {
            Bundle bundle = bpaUtils.createBundle(payload, destNodeID, false, true);
            logger.info("saving the bundle with ack flag, timestamp: " + bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS());
            // save to queue
            return bpaUtils.saveToQueue(bundle);
        }
        logger.warning("Unable to save the bundle to the queue with ack flag");
        return new Timestamp(DTNTime.getUnknownDTNTime(), -1);
    }

    /**
     * create the bundle and save to sending queue with Admin Report flag true
     * @param payload Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return key (timestamp) for the bundle else invalid timestamp
     */
    @Override
    public Timestamp sendWithAdminFlag(byte[] payload, NodeID destNodeID) {
        if(payload != null && payload.length > 0) {
            Bundle bundle = bpaUtils.createBundle(payload, destNodeID, true, false);
            logger.info("saving the bundle with admin flag, timestamp: " + bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS());
            // save to queue
            return bpaUtils.saveToQueue(bundle);
        }
        logger.warning("Unable to save the bundle to the queue with admin flag");
        return new Timestamp(DTNTime.getUnknownDTNTime(), -1);
    }

    /**
     * resend old bundle from queue
     * @param bundleTimestamp timestamp of the bundle to be resent
     * @return key (timestamp) for the bundle else invalid timestamp
     */
    @Override
    public Timestamp resendBundle(Timestamp bundleTimestamp) {
        if(bundleTimestamp.getSeqNum() != -1) {
            bpaUtils.saveToQueue(bundleStatusMap.get(bundleTimestamp).getBundle());
            logger.info("Resending bundle with timestamp " + bundleTimestamp.getCreationTime().getTimeInMS());
            return bundleTimestamp;
        }
        logger.warning("Unable to send bundle: " + bundleTimestamp.getCreationTime().getTimeInMS());
        return new Timestamp(DTNTime.getUnknownDTNTime(), -1);
    }
}

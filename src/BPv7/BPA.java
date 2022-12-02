package BPv7;


import BPv7.containers.*;
import BPv7.interfaces.BPAInterface;
import BPv7.utils.BundleDispatchStatusMap;
import BPv7.utils.DispatchStatus;
import BPv7.utils.StatusReportUtilObject;
import DTCP.DTCP;
import DTCP.interfaces.DTCPInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static BPv7.utils.DispatchStatus.*;

/**
 * Main class responsible for handling BP protocol and handling of bundles.
 * todo:: check {@link BPv7.interfaces.BPAInterface} for more comments on implementation requirements
 */
class BPA implements BPAInterface {//package-private (not private/public)
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(BPA.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static BPA instance = null;

    private Bundle createBundle(byte[] a,NodeID destID) {
        Bundle rem = new Bundle();
        PrimaryBlock PRB = new PrimaryBlock(destID, NodeID.getNullSourceID(), 500); //Lifetime how
        PayloadBlock PB = new PayloadBlock(a);
        rem.setPrimary(PRB);
        rem.setPayload(PB);
        return rem;
    }

    //actual class variables
    /**
     * todo:: comments
     */
    public static BlockingQueue<StatusReportUtilObject> sendStatusReportBuffer = new LinkedBlockingDeque<>();
    /**
     * todo:: comments
     */
    public static BlockingQueue<byte[]> readStatusReportBuffer = new LinkedBlockingDeque<>();
    /**
     * todo:: comments
     */
    protected static BlockingQueue<Bundle> sendBuffer = new LinkedBlockingDeque<>();
    /**
     * todo:: comments
     */
    protected static BlockingQueue<Bundle> receiveBuffer = new LinkedBlockingDeque<>();
    /**
     * todo:: comments
     */
    public static Map<Timestamp, BundleDispatchStatusMap> bundleStatusMap = new HashMap<>();
    /**
     * todo:: comments
     */
    private final Thread sendingThread;
    private final Thread receivingThread;



    //functions!

    /**
     * Gets the singleton instance of the BPA
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
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
     */
    protected BPA() {
        sendingThread = new Thread(new BPADispatcher());
        sendingThread.start();
        receivingThread = new Thread(new BPAReceiver());
        receivingThread.start();
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
     * Not required any more?
     * [blocking]
     * Gets the payload of the next admin bundle (which is just an admin record).
     *
     * TODO: need to understand if we will implement admin record
     * @return the payload of the next admin bundle
     */
    @Override
    public AdminRecord getAdminRecord() {
        // read from admin queue and pass to senderThread
        // Returns the payload of the next admin bundle (which is just an admin record)
        AdminRecord placeholder = new StatusReport(null, null, null,
                null, -1, null, null);
        //todo:: note that we may have other options for administrative records, need to be sure
        //  we parse the right one
        return placeholder;
    }

    /**
     * Returns the next bundle’s entire payload
     * @return byteStream of payload
     */
    public byte[] getPayload() {
        Bundle bundle = null;
        try {
            bundle = receiveBuffer.take();
            if(bundle != null && bundle.getPayload().getPayload() != null) {
                return bundle.getPayload().getPayload();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * create and save the bundle to outgoing queue
     * @param payload Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return timestamp of the bundle else invalid timestamp if unable to create or save the bundle to queue
     */
    public Timestamp send(byte[] payload, NodeID destNodeID) {
        if(payload != null && payload.length > 0) {
            Bundle bundle = createBundle(payload, destNodeID, false, false);
            // save to queue
            return saveToQueue(bundle);
        }
        return new Timestamp(DTNTime.getUnknownDTNTime(), -1);
    }

    /**
     * @param payload    Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return
     */
    @Override
    public Timestamp sendWithACK(byte[] payload, NodeID destNodeID) {
        if(payload != null && payload.length > 0) {
            Bundle bundle = createBundle(payload, destNodeID, false, true);
            // save to queue
            return saveToQueue(bundle);
        }
        return new Timestamp(DTNTime.getUnknownDTNTime(), -1);
    }

    /**
     * @param payload    Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return
     */
    @Override
    public Timestamp sendWithAdminFlag(byte[] payload, NodeID destNodeID) {
        if(payload != null && payload.length > 0) {
            Bundle bundle = createBundle(payload, destNodeID, true, false);
            // save to queue
            return saveToQueue(bundle);
        }
        return new Timestamp(DTNTime.getUnknownDTNTime(), -1);
    }

    /**
     * @param bundleTimestamp timestamp of the bundle to be resent
     * @return
     */
    @Override
    public Timestamp resendBundle(Timestamp bundleTimestamp) {
        if(bundleTimestamp.getSeqNum() != -1) {
            saveToQueue(bundleStatusMap.get(bundleTimestamp).getBundle());
            return bundleTimestamp;
        }
        return new Timestamp(DTNTime.getUnknownDTNTime(), -1);
    }

    /**
     * Util function to check status of bundle
     * @param key: bundle key
     * @return status of the bundle, NONE if invalid key
     */
    public DispatchStatus getBundleStatus(Timestamp key) {
        //DTN Epoch isn't the same as system Epoch, check DTNTime::timeInMS for specifics
        if(key.getSeqNum() != -1 && bundleStatusMap.containsKey(key)) {
            return bundleStatusMap.get(key).getStatus();
        }
        return NONE;
    }

    public Timestamp saveToQueue(Bundle bundle) {
        sendBuffer.add(bundle);
        Timestamp creationTimestamp = bundle.getPrimary().getCreationTimestamp();
        bundleStatusMap.put(creationTimestamp, new BundleDispatchStatusMap(bundle, PENDING));
        return creationTimestamp;
    }

    public Bundle createBundle(byte[] payload,NodeID destID, boolean adminFlag, boolean ackFlag) {
        Bundle bundle = new Bundle();
        PrimaryBlock primaryBlock = new PrimaryBlock(destID, NodeID.getNullSourceID(), 500); //Lifetime how
        if(adminFlag) {
            primaryBlock.setADMN();
        }
        if(ackFlag) {
            primaryBlock.setACKR();
        }
        PayloadBlock payloadBlock = new PayloadBlock(payload);
        bundle.setPrimary(primaryBlock);
        bundle.setPayload(payloadBlock);
        return bundle;
    }
}

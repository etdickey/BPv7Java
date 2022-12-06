package BPv7;


import BPv7.containers.*;
import BPv7.interfaces.BPAInterface;
import BPv7.utils.BundleStatus;
import BPv7.utils.StatusReportUtilObject;
import DTCP.DTCP;
import DTCP.interfaces.DTCPInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static BPv7.utils.BundleStatus.*;

/**
 * Main class responsible for handling BP protocol and handling of bundles.
 * todo:: check {@link BPv7.interfaces.BPAInterface} for more comments on implementation requirements
 */
class BPA implements BPAInterface {//package-private (not private/public)
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(BPA.class.getName());
    public static Object resendBundle;

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static BPA instance = null;

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
    private static Map<Integer, BundleStatus> bundleStatusMap = new HashMap<>();
    /**
     * todo:: comments
     */
    private final DTCPInterface dtcp = DTCP.getInstance();


    //functions!

    /**
     * Gets the singleton instance of the BPA
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this.instance volatile because its value only changes once
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
    protected BPA() {}

    /**
     * Send bundle from buffer/queue to DTCP
     * todo:: throw this sending code into a different class that extends Runnable
     *   (follows the principle of Separation of Concern in software engineering)
     */
    private void sendToDTCP() {
        new Thread(() -> {
            // passive sender thread: will spawn when we want to send a message
            // and only remain alive until all messages are sent.  More efficient
            // than having it busy-wait until a new message is ready to be sent.
            while(true) {
                Bundle bundleToSend = null;
                try {
                    bundleToSend = sendBuffer.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                NodeID destNode = bundleToSend.getPrimary().getDestNode();
                int timeInMS = bundleToSend.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS();
                if(dtcp.canReach(destNode)) {
                    if(dtcp.send(bundleToSend)) {
                        bundleStatusMap.put(timeInMS, SENT);
                    } else {
                        if(!canDelete(bundleToSend)) {
                            sendBuffer.add(bundleToSend);
                        } else {
                            bundleStatusMap.put(timeInMS, DELETED);
                        }
                    }
                }
            }
        }).start();
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
    public byte[] getPayload() throws InterruptedException {
        //todo:: switch to take()?
        Bundle bundle = sendBuffer.poll(20, TimeUnit.SECONDS);
        if(bundle != null) {
            return bundle.getPayload().getPayload();
        }
        return null;
    }

    /**
     * @param payload    Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return
     */
    @Override
    public int send(byte[] payload, NodeID destNodeID) {
        return 0;
    }

    /**
     * @param payload    Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return
     */
    @Override
    public int sendWithACK(byte[] payload, NodeID destNodeID) {
        return 0;
    }

    /**
     * @param payload    Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return
     */
    @Override
    public int sendWithAdminFlag(byte[] payload, NodeID destNodeID) {
        return 0;
    }

    /**
     * @param bundleTimestamp timestamp of the bundle to be resent
     * @return
     */
    @Override
    public int resendBundle(Timestamp bundleTimestamp) {
        return 0;
    }

    /**
     * saves the bundle to queue
     * @param bundle bundle from User API
     * @return -1 if unable to save the bundle, else key (timestamp) for the bundle
     */
    public int send(Bundle bundle) {
        if(bundle != null) {
            // save to queue
            sendBuffer.add(bundle);
            int timeInMS = bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS();
            bundleStatusMap.put(timeInMS, PENDING);
            return timeInMS;
        }
        return -1;
    }

    /**
     * Util function to check status of bundle
     * @param key: bundle key
     * @return status of the bundle, NONE if invalid key
     */
    public BundleStatus getBundleStatus(int key) {
        //DTN Epoch isn't the same as system Epoch, check DTNTime::timeInMS for specifics
        if(key > 0 && key <= DTNTime.getCurrentDTNTime().timeInMS && bundleStatusMap.containsKey(key)) {
            return bundleStatusMap.get(key);
        }
        return NONE;
    }

    /**
     * Checks if bundle can be deleted based on LifeTime set in Primary block
     * @param bundle: bundle to be checked
     * @return boolean(), true if we can delete it, else false.
     */
    private boolean canDelete(Bundle bundle) {
        long timeGap = Math.subtractExact(System.currentTimeMillis(), bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS());
        return timeGap > bundle.getPrimary().getLifetime();
    }
}

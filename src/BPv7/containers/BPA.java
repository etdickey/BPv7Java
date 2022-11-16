package BPv7.containers;


import BPv7.interfaces.BPAInterface;
import DTCP.containers.DTCP;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static BPv7.containers.BundleStatus.*;

/**
 * Main class responsible for handling BP protocol and handling of bundles.
 */
public class BPA implements BPAInterface {
    /**
     * todo
     */
    protected Queue<Bundle> adminQueue = new PriorityQueue<>();
    /**
     * todo:: lock this down?
     */
    protected BlockingQueue<Bundle> sendBuffer = new LinkedBlockingDeque<>();
    /**
     * todo
     */
    private Map<Integer, BundleStatus> bundleStatusMap = new HashMap<>();
    /**
     * todo
     */
    private DTCP dtcp = new DTCP();

    // TODO: implement sending from buffer to DTCP (sender thread)

    /**
     * TODO: need to understand if we will implement admin record
     * @return
     */
    public StatusReport getAdminRecord() {
        // read from admin queue and pass to senderThread
        // Returns the payload of the next admin bundle (which is just an admin record)
        StatusReport placeholder = new StatusReport(null, null, null,
                null, -1, null, null);
        return placeholder;
    } // ::[blocking]

    /**
     * Returns the next bundle’s entire payload
     * @return byteStream of payload
     */
    public byte[] getPayload() throws InterruptedException {
        // TODO: read from config file
        Bundle bundle = sendBuffer.poll(20, TimeUnit.SECONDS);
        if(bundle != null) {
            return bundle.getPayload().getPayload().getBytes();
        }
        return null;
    }

    /**
     * saves the bundle to queue
     * @param bundle: bundle from User API
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

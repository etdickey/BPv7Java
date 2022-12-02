package BPv7;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;
import BPv7.containers.Timestamp;
import BPv7.utils.BundleDispatchStatusMap;
import DTCP.DTCP;
import DTCP.interfaces.DTCPInterface;

import java.util.logging.Logger;

import static BPv7.BPA.*;
import static BPv7.utils.DispatchStatus.DELETED;
import static BPv7.utils.DispatchStatus.SENT;

public class BPADispatcher implements Runnable {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(BPADispatcher.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static BPADispatcher instance = null;
    /**
     * dtcp Instance for using functions
     */
    private final DTCPInterface dtcp = DTCP.getInstance();


    //functions!

    /**
     * Gets the singleton instance of the BPADispatcher
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static BPADispatcher getInstance() {
        if(instance == null){
            synchronized (BPADispatcher.class) {
                if(instance == null) {
                    instance = new BPADispatcher();
                    logger.info("Created BPADispatcher singleton");
                }
            }
        }
        return instance;
    }

    protected BPADispatcher() {}

    /**
     * passive sender thread: will spawn when we want to send a message and only remain alive until all messages are sent.
     * More efficient than having it busy-wait until a new message is ready to be sent. Checks if destination node ID
     * can be reached and tries to send it. If sending fails, deleting is attempted. If deleted, bundleStatusMap is updated,
     * otherwise the bundle is re-added to the sendBuffer.
     */
    @Override
    public void run() {

        while(true) {
            Bundle bundleToSend = null;
            try {
                bundleToSend = sendBuffer.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            NodeID destNode = bundleToSend.getPrimary().getDestNode();
            Timestamp creationTimestamp = bundleToSend.getPrimary().getCreationTimestamp();
            if(dtcp.canReach(destNode)) {
                if(dtcp.send(bundleToSend)) {
                    bundleStatusMap.put(creationTimestamp, new BundleDispatchStatusMap(bundleToSend, SENT));
                } else {
                    if(!canDelete(bundleToSend)) {
                        sendBuffer.add(bundleToSend);
                    } else {
                        bundleStatusMap.put(creationTimestamp, new BundleDispatchStatusMap(bundleToSend, DELETED));
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
        long timeGap = Math.subtractExact(System.currentTimeMillis(), bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS());
        return timeGap > bundle.getPrimary().getLifetime();
    }
}

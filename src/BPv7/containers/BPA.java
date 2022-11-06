package BPv7.containers;


import DTCP.interfaces.DTCP;

import java.util.PriorityQueue;
import java.util.Queue;

public class BPA implements BPv7.interfaces.BPA {

    Queue<Bundle> adminQueue = new PriorityQueue<>();
    Queue<Bundle> sendBuffer = new PriorityQueue<>();
    DTCP dtcp = new DTCP.containers.DTCP();

    public StatusReport getAdminRecord() {
        // read from admin queue and pass to senderThread
        // Returns the payload of the next admin bundle (which is just an admin record)
        StatusReport placeholder = new StatusReport();
        return placeholder;
    } // ::[blocking]

    public byte[] getPayload() {
        /*Returns the next bundle’s entire payload*/
        byte[] abc = java.util.HexFormat.of().parseHex("e04fd020ea3a6910a2d808002b30309d");
        return abc; /*variable and return placeholder for required return*/
    }

    /**
     * saves the bundle to queue and then sends bundles from queue to DTCP
     * @param bundle: bundle from User API
     * @return: -1 if unable to send the bundle, else 1
     */
    public int send(Bundle bundle) {
        if(bundle != null) {
            // save to queue
            sendBuffer.add(bundle);
            // send bundles from queue
            while(!sendBuffer.isEmpty()) {
                Bundle bundleToSend = sendBuffer.poll();
                NodeID destNode = bundleToSend.getPrimary().getDestNode();
                if (dtcp.canReach(destNode)) {
                    if(dtcp.send(bundleToSend)) {
                        return 1;
                    } else {
                        // check if we can delete the bundle, if not add back of the queue
                        if(!canDelete(bundleToSend)) {
                            sendBuffer.add(bundleToSend);
                        }
                    }
                } else {
                    if(!canDelete(bundleToSend)) {
                        sendBuffer.add(bundleToSend);
                    }
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Checks if bundle can be delete based on LifeTime set in Primary block
     * @param bundle: bundle to be checked
     * @return: boolean(), true if we can delete it, else false.
     */
    private boolean canDelete(Bundle bundle) {
        long timeGap = Math.subtractExact(System.currentTimeMillis(), bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS());
        return timeGap <= bundle.getPrimary().getLifetime() ? false : true;
    }

    /*Notes on BPA:
Maintains two buffers, one of admin bundles and one of normal bundles
Needs asynchronous function that waits for new bundles
Not declaring buffers as java seems to need buffer type and I'm unsure if its ByteBuffer type
*/
}

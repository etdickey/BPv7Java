package BPv7.containers;


import DTCP.containers.DTCP;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BPA implements BPv7.interfaces.BPA {

    public Queue<Bundle> adminQueue = new PriorityQueue<>();
    public BlockingQueue<Bundle> sendBuffer = new LinkedBlockingDeque<>();
    public AtomicInteger bundleKey = new AtomicInteger(0);
    private DTCP dtcp = new DTCP();

    // TODO: implement sending from buffer to DTCP (sender thread)

    /**
     * TODO: need to understand if we will implement admin record
     * @return
     */
    public StatusReport getAdminRecord() {
        // read from admin queue and pass to senderThread
        // Returns the payload of the next admin bundle (which is just an admin record)
        StatusReport placeholder = new StatusReport();
        return placeholder;
    } // ::[blocking]

    /**
     * Returns the next bundle’s entire payload
     * @return byteStream of payload
     */
    public byte[] getPayload() throws InterruptedException {
        Bundle bundle = sendBuffer.poll(20, TimeUnit.SECONDS);
        if(bundle != null) {
            return bundle.getPayload().getPayload().getBytes();
        }
        return null;
    }

    /**
     * saves the bundle to queue
     * @param bundle: bundle from User API
     * @return: -1 if unable to save the bundle, else key (counter value) for the bundle in the buffer
     */
    public int send(Bundle bundle) {
        if(bundle != null) {
            // save to queue
            sendBuffer.add(bundle);
            return bundleKey.incrementAndGet();
        }
        return -1;
    }

    /**
     * Checks if bundle can be deleted based on LifeTime set in Primary block
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

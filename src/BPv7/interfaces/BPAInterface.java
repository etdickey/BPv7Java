package BPv7.interfaces;

import BPv7.containers.AdminRecord;
import BPv7.containers.Bundle;

/**
 * Defines the API for the BPA!
 */
public interface BPAInterface {
    //listener thread
    //   note: discard any sequence of bytes that does not conform to BP

    //todo:: bundle parse

    //todo: defragment functionality, gets all the fragment bundles and gets their
    // payload and combines it into a single byte stream and feeds it back
    // into the bundle parser

    /**
     * [blocking]
     * Gets the payload of the next admin bundle (which is just an admin record).
     *
     * @return the payload of the next admin bundle
     */
    AdminRecord getAdminRecord();

    /**
     * Returns the next bundle’s entire payload
     * @return byteStream of payload
     */
    byte[] getPayload() throws InterruptedException;

    /**
     * saves the bundle to queue
     * @param bundle bundle from User API
     * @return -1 if unable to save the bundle, else key (timestamp) for the bundle
     */
    int send(Bundle bundle);
}

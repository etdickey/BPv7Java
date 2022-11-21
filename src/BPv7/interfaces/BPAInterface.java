package BPv7.interfaces;

import BPv7.containers.AdminRecord;
import BPv7.containers.Bundle;
import BPv7.containers.NodeID;
import BPv7.containers.Timestamp;

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
     * create the bundle and save to sending queue
     * @param payload Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return -1 if unable to create/save the bundle, else key (timestamp) for the bundle
     */
    int send(byte[] payload, NodeID destNodeID);

    /**
     * create the bundle and save to sending queue with ACK flag true
     * @param payload Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return -1 if unable to create/save the bundle, else key (timestamp) for the bundle
     */
    int sendWithACK(byte[] payload, NodeID destNodeID);

    /**
     * create the bundle and save to sending queue with Admin Report flag true
     * @param payload Payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return -1 if unable to create/save the bundle, else key (timestamp) for the bundle
     */
    int sendWithAdminFlag(byte[] payload, NodeID destNodeID);

    /**
     * resend old bundle from queue
     * @param bundleTimestamp timestamp of the bundle to be resent
     * @return -1 if unable to resend the bundle, else key (timestamp) for the bundle
     */
    int resendBundle(Timestamp bundleTimestamp);
}

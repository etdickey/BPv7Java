package BPv7.interfaces;

import BPv7.containers.NodeID;
import BPv7.containers.Timestamp;
import BPv7.utils.DispatchStatus;

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
     * [blocking call]
     * Gets the payload of the next admin bundle (which is just an admin record).
     * @return the payload of the next admin bundle
     */
    byte[] getAdminRecord();

    /**
     * [blocking call]
     * Returns the next bundle’s entire payload
     * @return byteStream of payload
     */
    byte[] getPayload();

    /**
     * create the bundle and save to sending queue
     * @param payload message to send in payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return key (timestamp) for the bundle
     */
    Timestamp send(byte[] payload, NodeID destNodeID);

    /**
     * create the bundle and request an ACK from [the next hop, the final destination]
     * @param payload message to send in payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return key (timestamp) for the bundle
     */
    Timestamp sendWithACK(byte[] payload, NodeID destNodeID);

    /**
     * create the admin bundle and save to sending queue
     * @param payload message to send in payload block of the bundle
     * @param destNodeID destination node id of the bundle
     * @return key (timestamp) for the bundle
     */
    Timestamp sendWithAdminFlag(byte[] payload, NodeID destNodeID);

    /**
     * resend old bundle from queue
     * @param bundleTimestamp timestamp of the bundle to be resent
     * @return key (timestamp) for the bundle
     */
    Timestamp resendBundle(Timestamp bundleTimestamp);

    DispatchStatus getBundleStatus(Timestamp bundleTimestamp);
}

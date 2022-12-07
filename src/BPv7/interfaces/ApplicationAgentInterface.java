package BPv7.interfaces;

import BPv7.containers.NodeID;
import BPv7.containers.Timestamp;

/**
 * Defines the interface for interacting with BP from the application-level perspective
 */
public interface ApplicationAgentInterface {
    /**
     * Simple wrapper to tell the receiver who sent the payload
     * @param payload byte[] payload of bundle
     * @param sender NodeID who sent the payload
     */
    record ReceivePackage(byte[] payload, NodeID sender) {}

    /**
     * [blocking] Sends a message to the BPA to bundle and send to the end destination
     * @param packetToSend message to send
     * @return packet(bundle) timestamp
     */
    Timestamp send(byte[] packetToSend, NodeID destNodeID);

    /**
     * Returns the next message from the stream.
     * Calls BPA::getPayload once and saves all the payload in a buffer to return to user at some point
     *
     * @param numToRead number of bytes to read from the stream
     * @return byte[] of size numToRead and sender NodeID (ReceivePackage)
     * @throws InterruptedException if unable to read next payload
     */
    ReceivePackage read(int numToRead) throws InterruptedException;
    /**
     * Checks if the packet ID passed in has been sent to the next hop
     * Will trash record of complete transmission after >=1 minute (config file)
     * @param packetTimestamp timestamp of packet to check
     * @return true if the “packet” has reached the next hop ONLY (else false)
     */
    boolean checkSent(Timestamp packetTimestamp);
}

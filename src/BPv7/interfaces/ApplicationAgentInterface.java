package BPv7.interfaces;

import BPv7.containers.NodeID;

/**
 * Defines the interface for interacting with BP from the application-level perspective
 */
public interface ApplicationAgentInterface {
    /**
     * [blocking] Sends a message to the BPA to bundle and send to the end destination
     * @param packetToSend message to send
     * @return packet ID or -1 if packetToSend is Null or BP layer is full
     */
    // int send(byte[] packetToSend);

    int send(byte[] packetToSend, NodeID destNodeID);

    /**
     * Returns the next message from the stream.
     * Calls BPA::getPayload once and saves all the payload in a buffer to return to user at some point
     * @param numToRead number of bytes to read from the stream
     * @return byte[] of size numToRead
     */
    byte[] read(int numToRead) throws InterruptedException;
    /**
     * Checks if the packet ID passed in has been sent to the next hop
     * Will trash record of complete transmission after >=1 minute (config file)
     * @param packetID ID of packet to check
     * @return true if the “packet” has reached the next hop ONLY (else false)
     */
    boolean checkSent(int packetID);
}

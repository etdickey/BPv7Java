package DTCP.interfaces;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;

/**
 * Interface defining any interaction between the BPA (or any class using DTCP) and DTCP
 */
public interface DTCPInterface {
    /**
     * Sends bundle to bundle node
     * @param toBeSent bundle to be sent
     * @return true if successful, else false
     */
    boolean send(Bundle toBeSent);

    /**
     * Checks the network status only for PREDICTABLE disruptions and if we actually have a connection to that NodeID
     * @param ID NodeID of the network
     * @return true if network is up else false
     */
    boolean canReach(NodeID ID);

    /**
     * Find the networkID for the given node
     * @param ID node object
     * @return networkId of the node
     */
    String nodeToNetwork(NodeID ID);
}

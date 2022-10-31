package DTCP.containers;

import BPv7.interfaces.Bundle;
import BPv7.interfaces.NodeID;

public class DTCP implements DTCP.interfaces.DTCP {

    /**
     * Sends bundle to bundle node
     * @param toBeSent: bundle to be sent
     * @return true if successful, else false
     */
    @Override
    public boolean send(Bundle toBeSent) {
        //TODO: implement sending logic
        return false;
    }

    /**
     * Checks the network status
     * @param ID: NodeID of the network
     * @return true if network is up else false
     */
    @Override
    public boolean canReach(NodeID ID) {
        //TODO: find network health status and return status
        return false;
    }

    /**
     * Find the networkID for the given node
     * @param ID: node object
     * @return networkId of the node
     */
    @Override
    public String nodeToNetwork(NodeID ID) {
        //TODO: get networkID from the Node object
        return "[placeholder] NetworkID";
    }


}

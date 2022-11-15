package DTCP.interfaces;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;

/*
To Do:
Add required parameters
Add all functions
 */
public interface DTCPInterface {
    boolean send(Bundle toBeSent);
    boolean canReach(NodeID ID);
    String nodeToNetwork(NodeID ID);
}

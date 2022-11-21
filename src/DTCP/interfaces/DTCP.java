package DTCP.interfaces;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;

/*
To Do:
Add required parameters
Add all functions
 */
public interface DTCP {
    boolean send(Bundle toBeSent);
    Bundle recv();
    boolean canReach(NodeID ID);
    String nodeToNetwork(NodeID ID);
}

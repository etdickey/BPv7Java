package DTCP.interfaces;

import BPv7.interfaces.Bundle;
import BPv7.interfaces.NodeID;

/*
To Do:
Add required parameters
Add all functions
 */
public interface DTCP {
    boolean send(Bundle toBeSent);
    boolean canReach(NodeID ID);
    private String nodeToNetwork(NodeID ID) {
        return "placeholder";
    }
}

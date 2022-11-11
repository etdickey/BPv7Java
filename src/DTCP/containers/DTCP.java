package DTCP.containers;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;

public class DTCP implements DTCP.interfaces.DTCP {


    // Needed class variables:
    Map<int, String> idToAddressMap; //Types may need to be changed
    // Edge List, maybe with a PRNG list with seeds? talk to group about it.
    // i think thats it? since it doesn't have to do routing.


    /**
     * Sends bundle to bundle node
     * @param toBeSent: bundle to be sent
     * @return true if successful, else false
     */
    @Override
    public boolean send(Bundle toBeSent) {
        //TODO: implement sending logic

        /*
         * Structure:
         *  - This should really just be:
         *      + pull out destination from Bundle (its in the primary block)
         *      + Makes sure it can reach it
         *      + Return false if a path isn't found, otherwise send it over TCP to the IP:port gotten from nodeToNetwork
         */

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

        /* 
         * Structure:
         *  - Determining if a link is up or not needs to be pseudo-random, cause both sides need to know if its down 
         *    or not, so I/we will need to come up with a seed scheme I guess involving both nodes, and a time period,
         *    and have it redetermine on that time period. All members of this network will need the same result for 
         *    each, so probably PRNG + Config determining how we use that.
         *  - Otherwise, this just checks if (a) there is a direct connection to that node, and (b) if its currently distrupted
         */

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

        /*  
         * Structure:
         *  - Read in local map of NodeID to networkID from config file
         *  - Just look it up in the map
         * Questions:
         *  - NodeID is a URI I believe, based on 4.2.5
         *  - Why is this public, assumedly its a URI/NodeID to IP address:port map, no one else needs to know it?
         */

        return "[placeholder] NetworkID";
    }


}

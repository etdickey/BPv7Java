package DTCP.containers;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;

import java.time.Instant;
import java.util.Map;
import java.util.Random;

public class DTCP implements DTCP.interfaces.DTCP {


    // Needed class variables:
    // Edge List, maybe with a PRNG list with seeds? talk to group about it.
    // I think that's it? since it doesn't have to do routing.


    private Map<String, String> idToAddressRoutingMap; //Types may need to be changed
    private String thisAddress;
    private int milliPerPeriod; //1-1000
    private int downChance;
    private int totalChance;

    public DTCP() {

    }

    /**
     * The function for receiving from other nodes
     * @return the Bundle received
     */
    public Bundle recv() {
        return null;
    }

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
     *
     * @param ID the URI of the destination
     * @return The IP address of the destination node
     */
    private String nodeToString(NodeID ID) {
        return "";
    }

    /**
     * Checks the network status only for PREDICTABLE disruptions and if we actually have a connection to that NodeID
     * @param ID: NodeID of the network
     * @return true if network is up else false
     */
    @Override
    public boolean canReach(NodeID ID) {

        /*
         * Structure:
         *  - Determining if a link is up or not needs to be pseudo-random, cause both sides need to know if its down
         *    or not, so I/we will need to come up with a seed scheme I guess involving both nodes, and a time period,
         *    and have it redetermine on that time period. All members of this network will need the same result for
         *    each, so probably PRNG + Config determining how we use that.
         *  - Otherwise, this just checks if (a) there is a direct connection to that node, and (b) if its currently distrupted
         */

        String dest = nodeToNetwork(ID);
        if (dest == null)
            return false;
        return isConnectionDownExpected(dest);
    }

    /**
     * Gets the current rounded timeframe
     * @return Get the current rounded time frame for the current connection
     */
    private long getCurrentTimeFrame() {
        Instant now = Instant.now();
        long timeframe = 0;
        timeframe += now.getEpochSecond() * 1000;
        timeframe += ((now.toEpochMilli() % 1000) / (milliPerPeriod)) * milliPerPeriod;
        return timeframe;
    }

    /**
     * Convert a String IP Address to a long. Used for Randomization
     * @param address the IP address to convert
     * @return the long version of the ip address as an int.
     */
    private long addressToLong(String address) {
        String[] part = address.split("\\.");
        long num = 0;
        for (int i = 0; i < part.length; i++) {
            int power = 3 - i;
            num += ((Integer.parseInt(part[i]) % 256 * Math.pow(256, power)));
        }
        return num;
    }

    /**
     * Get if the connection to the destination address after routing is expected to be down
     * @param destAddress the address of the next hop
     * @return true if the connection is expected to be down, otherwise false
     */
    private boolean isConnectionDownExpected(String destAddress) {
        long thisAddr = addressToLong(thisAddress);
        long destAddr = addressToLong(destAddress);
        if (destAddr == thisAddr)
            return false;
        long seed;
        seed = thisAddr ^ destAddr ^ getCurrentTimeFrame();
        Random generator = new Random(seed);
        int res = generator.nextInt(totalChance);
        return res < downChance;
    }

    /**
     * Find the networkID for the given node
     * @param ID: node object
     * @return networkId of the node
     */
    @Override
    public String nodeToNetwork(NodeID ID) {
        //TODO: get networkID from the Node object

        return idToAddressRoutingMap.getOrDefault(nodeToString(ID), null);
    }


}

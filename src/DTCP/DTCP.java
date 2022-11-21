package DTCP;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;
import Configs.ConvergenceLayerParams;
import DTCP.interfaces.DTCPInterface;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements all DTCP procedures (disruptions and TCP interaction)
 */
public class DTCP implements DTCPInterface {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(DTCP.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static DTCP instance = null;


    //Local variables:
    /**
     * The instance of the ConvergenceLayerParams Class
     */
    private final ConvergenceLayerParams config;

    /**
     * The queue for bundles received to offer to the BPA layer
     */
    private final LinkedBlockingQueue<Bundle> outQueue;


    /**
     * Gets the singleton instance of DTCP
     *
     * @return a reference to the DTCP instance
     * @implNote not making this.instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static DTCPInterface getInstance(){
        if(instance == null){
            synchronized (DTCP.class){
                if(instance == null){
                    instance = new DTCP();
                    logger.info("Created DTCP singleton");
                }
            }
        }
        return instance;
    }

    /**
     * Hiding default constructor to force singleton use
     */
    protected DTCP(){
        //useful information stored in
        config = ConvergenceLayerParams.getInstance();
        if (config.queueCapacity != -1)
            outQueue = new LinkedBlockingQueue<>(config.queueCapacity);
        else
            outQueue = new LinkedBlockingQueue<>();
        //TODO: Setup internal thread for receiving

        //todo: set up useful stuff (@Aidan)
    }

    /**
     * Sends bundle to bundle node
     * @param toBeSent bundle to be sent
     * @return true if successful, else false
     */
    @Override
    public boolean send(Bundle toBeSent) {
        //TODO: implement sending logic

        /*
         * Structure:
         *  - This should really just be:
         *      + pull out destination from Bundle (it's in the primary block)
         *      + Makes sure it can reach it
         *      + Return false if a path isn't found, otherwise send it over TCP to the IP:port gotten from nodeToNetwork
         */



        return false;
    }

    /**
     * This function is used to receive a bundle that was sent to this node. It is blocking.
     * @return A bundle sent to this node
     */
    public Bundle recv() {
        try {
            return outQueue.take();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Queue was interrupted: " + e.getMessage()); //May need to change log level
            return null;
        }
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
     * @param ID NodeID of the network
     * @return true if network is up else false
     */
    @Override
    public boolean canReach(NodeID ID) {

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
        timeframe += ((now.toEpochMilli() % 1000) / (config.milliPerDownPeriod)) * config.milliPerDownPeriod;
        return timeframe;
    }

    /**
     * Get if the connection to the destination address after routing is expected to be down
     * @param destAddress the address of the next hop
     * @return true if the connection is expected to be down, otherwise false
     */
    private boolean isConnectionDownExpected(String destAddress) {
        long thisAddr = addressToLong(config.thisAddress);
        long destAddr = addressToLong(destAddress);
        if (destAddr == thisAddr)
            return false;
        long seed;
        seed = thisAddr ^ destAddr ^ getCurrentTimeFrame();
        Random generator = new Random(seed);
        int res = generator.nextInt(config.totalChance);
        return res < config.expectedDownChance;
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
     * Find the networkID for the given node
     * @param ID node object
     * @return networkId of the node
     */
    @Override
    public String nodeToNetwork(NodeID ID) {
        return config.idToAddressRoutingMap.getOrDefault(nodeToString(ID), null);
    }


}

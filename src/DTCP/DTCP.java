package DTCP;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;
import Configs.ConvergenceLayerParams;
import DTCP.interfaces.DTCPInterface;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
     * The queue for bundles received to offer to the BPA layer.
     * This is package private for ClientHandler.
     */
    final BlockingQueue<Bundle> outQueue;

    /**
     * The server thread. Keeping it in case we need it later for checking status and such
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final Thread server;


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
     * Hiding default constructor to force singleton use. Sets up the config, the input queue, then the receiving server
     */
    protected DTCP(){
        //useful information stored in
        config = ConvergenceLayerParams.getInstance();
        if (config.queueCapacity != -1)
            outQueue = new LinkedBlockingQueue<>(config.queueCapacity);
        else
            outQueue = new LinkedBlockingQueue<>();
        server = new Thread(new DTCPServer(outQueue));
        server.start();
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
     * Checks the network status only for PREDICTABLE disruptions and if we actually have a connection to that NodeID
     * @param ID NodeID of the network
     * @return true if network is up else false
     */
    @Override
    public boolean canReach(NodeID ID) {

        String dest = nodeToNetwork(ID);
        if (dest == null)
            return false;
        return DTCPUtils.isConnectionDownExpected(dest);
    }

    /**
     * Find the networkID for the given node
     * @param ID node object
     * @return networkId of the node
     */
    @Override
    public String nodeToNetwork(NodeID ID) {
        return config.idToAddressRoutingMap.getOrDefault(DTCPUtils.nodeToString(ID), null);
    }




}

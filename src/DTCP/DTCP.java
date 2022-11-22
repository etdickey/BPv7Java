package DTCP;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;
import Configs.ConvergenceLayerParams;
import DTCP.interfaces.DTCPInterface;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InvalidPropertiesFormatException;
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
        //noinspection DoubleCheckedLocking
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
        String loggingID = DTCPUtils.getLoggingBundleId(toBeSent);
        NodeID destNode = toBeSent.getPrimary().getDestNode();
        if (!canReach(destNode)) {
            logger.log(Level.WARNING, "Attempted to send to unreachable destination. BundleID: " + loggingID);
            return false;
        }
        String dest = nodeToNetwork(destNode);
        byte[] bundleAsBytes;
        try {
            bundleAsBytes = toBeSent.getNetworkEncoding();
        } catch (InvalidPropertiesFormatException e) {
            logger.log(Level.WARNING, "Attempted to send a bundle with invalid properties. BundleID: " + loggingID);
            return false;
        }
        try (Socket socket = new Socket(dest, config.DTCP_Port)) {
            socket.getOutputStream().write(bundleAsBytes);
            logger.log(Level.INFO, "Successfully sent bundle. BundleID: " + loggingID);
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, "Failed to find destination host of Bundle. BundleID: " + loggingID);
            return false;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to send bundle over socket. BundleID: " + loggingID);
            return false;
        }
        return true;
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
     * @implNote This doesn't seem to need to be public
     * @param ID node object
     * @return networkId of the node
     */
    @Override
    public String nodeToNetwork(NodeID ID) {
        return config.idToAddressRoutingMap.getOrDefault(ID.id(), null);
    }




}

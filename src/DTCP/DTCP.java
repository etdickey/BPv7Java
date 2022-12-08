package DTCP;

import BPv7.containers.Bundle;
import BPv7.containers.DTNTime;
import BPv7.containers.NodeID;
import Configs.ConvergenceLayerParams;
import Configs.SimulationParams;
import DTCP.interfaces.DTCPInterface;
import DTCP.interfaces.ReachableStatus;

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
    private static final ConvergenceLayerParams convParams = ConvergenceLayerParams.getInstance();

    /**
     * The instance of the SimulationParams Class
     */
    private static final SimulationParams simParams = SimulationParams.getInstance();

    /**
     * The queue for bundles received to offer to the BPA layer.
     */
    private final BlockingQueue<Bundle> outQueue;

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
        if (convParams.queueCapacity != -1)
            outQueue = new LinkedBlockingQueue<>(convParams.queueCapacity);
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
        String loggingID = toBeSent.getLoggingBundleId(); // For logging, mostly arbitrary
        NodeID destNode = toBeSent.getPrimary().getDestNode();
        ReachableStatus status = canReach(destNode);
        if (status == ReachableStatus.EXPECTED_DOWN) {
            // This (likely) means the status changed since they last checked, just let them know it didn't make it
            logger.log(Level.INFO, "Attempted to send a bundle during an expected down, potentially just a timing.");
            return false;
        }
        if (status != ReachableStatus.REACHABLE) {
            // This means something really wrong happened, because it was never reachable in the first place
            logger.log(Level.WARNING, "Attempted to send to unreachable destination. BundleID: " + loggingID);
            return false;
        }
        String dest = nodeToNetwork(destNode); //Should be IPv4
        byte[] bundleAsBytes;
        try {
            // Get the CBOR converted Byte Array
            bundleAsBytes = toBeSent.getNetworkEncoding(logger);

        } catch (InvalidPropertiesFormatException e) {
            // Something is wrong with the bundle, not DTCPs fault, so just drop it
            logger.log(Level.WARNING, "Attempted to send a bundle with invalid properties. BundleID: " + loggingID);
            return false;
        }
        try (Socket socket = new Socket(dest, simParams.scenario.dtcpPort())) {
            // Just write the whole bundle
            socket.getOutputStream().write(bundleAsBytes);
            logger.log(Level.INFO, "[NetStats] Bundle Sent: " + loggingID
                                        + "; Time (ms) since creation: " + (DTNTime.getCurrentDTNTime().timeInMS - toBeSent.getPrimary().getCreationTimestamp().creationTime().timeInMS)
                                        + "; Size of bundle payload (bytes):" + toBeSent.getPayload().getPayload().length);
        } catch (UnknownHostException e) {
            // Something is wrong on internet network backside, not our problem, drop it
            logger.log(Level.WARNING, "Failed to find destination host of Bundle. BundleID: " + loggingID);
            return false;
        } catch (IOException e) {
            // Something happened (probably on OS side) that made this fail, not our problem, drop it
            logger.log(Level.WARNING, "Failed to send bundle over socket <ip: " + dest
                    + ", port: " + simParams.scenario.dtcpPort() + ">. BundleID: " + loggingID + ":: Exception = "
                    + e.getMessage());
            return false;
        }
        // Should have been sent by now
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
     * @return ReachableStatus of the NodeID
     */
    @Override
    public ReachableStatus canReach(NodeID ID) {
        String dest = nodeToNetwork(ID);
        if (dest == null || !convParams.idToAddressRoutingMap.containsKey(dest))
            return ReachableStatus.UNKNOWN_ID;
        if (convParams.idToAddressRoutingMap.get(dest) == null)
            return ReachableStatus.NO_ROUTE;
        if (DTCPUtils.isConnectionDownExpected(dest))
            return ReachableStatus.EXPECTED_DOWN;
        return ReachableStatus.REACHABLE;
    }

    /**
     * Find the networkID for the given node
     * @param ID node object
     * @return networkId of the node
     */
    @Override
    public String nodeToNetwork(NodeID ID) {
        if (ID.equals(NodeID.getNullSourceID()))
            return null;
        return convParams.idToAddressRoutingMap.getOrDefault(ID.id(), null);
    }




}

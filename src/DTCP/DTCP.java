package DTCP;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;
import Configs.ConvergenceLayerParams;
import DTCP.interfaces.DTCPInterface;

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
        ConvergenceLayerParams params = ConvergenceLayerParams.getInstance();
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
        return false;
    }

    /**
     * Checks the network status only for PREDICTABLE disruptions and if we actually have a connection to that NodeID
     * @param ID NodeID of the network
     * @return true if network is up else false
     */
    @Override
    public boolean canReach(NodeID ID) {
        //TODO: find network health status and return status
        return false;
    }

    /**
     * Find the networkID for the given node
     * @param ID node object
     * @return networkId of the node
     */
    @Override
    public String nodeToNetwork(NodeID ID) {
        //TODO: get networkID from the Node object
        return "[placeholder] NetworkID";
    }


}

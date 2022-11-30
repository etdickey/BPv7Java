package BPv7;

import BPv7.containers.Bundle;
import BPv7.containers.NodeID;
import BPv7.containers.Timestamp;
import BPv7.utils.StatusReportUtilObject;
import Configs.SimulationParams;
import DTCP.DTCP;
import DTCP.interfaces.DTCPInterface;

import java.util.logging.Logger;

import static BPv7.BPA.*;
import static BPv7.utils.BundleStatusReport.DELIVERED;
import static BPv7.utils.BundleStatusReport.FORWARDED;

public class BPAReceiver implements Runnable {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(BPAReceiver.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static BPAReceiver instance = null;
    /**
     * todo:: comments
     */
    private final DTCPInterface dtcp = DTCP.getInstance();
    private final SimulationParams simulationParams = SimulationParams.getInstance();


    //functions!

    /**
     * Gets the singleton instance of the BPADispatcher
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static BPAReceiver getInstance() {
        if(instance == null){
            synchronized (BPAReceiver.class) {
                if(instance == null) {
                    instance = new BPAReceiver();
                    logger.info("Created BPAReceiver singleton");
                }
            }
        }
        return instance;
    }

    protected BPAReceiver() {}

    /**
     *
     */
    @Override
    public void run() {
        while(true) {
            Bundle bundle = dtcp.recv();
            // TODO: @ethan get id of current NodeID
            // read nodeID of system and see if matches bundle destination ID
            // TODO: change if condition
            if(bundle.getPrimary().getDestNode().id() == "1") {
                // if bundle has admin flag set
                // add the payload to readStatusReportBuffer
                if ((bundle.getPrimary().getFlags() & 0x02) == 1) {
                    readStatusReportBuffer.add(bundle.getPayload().getPayload());
                } else {
                    // else bundle has ack flag set
                    // make a StatusReportUtilObject object add to sendStatusReportBuffer
                    if((bundle.getPrimary().getFlags() & 0x20) == 1) {
                        NodeID nodeID = bundle.getPrimary().getDestNode();
                        Timestamp timestamp = bundle.getPrimary().getCreationTimestamp();
                        sendStatusReportBuffer.add(new StatusReportUtilObject(nodeID, timestamp, DELIVERED));
                    }
                    // add bundle to receiveBuffer
                    receiveBuffer.add(bundle);
                }
            } else {
                // check if bundle has ack flag
                if((bundle.getPrimary().getFlags() & 0x20) == 1) {
                    NodeID nodeID = bundle.getPrimary().getDestNode();
                    Timestamp timestamp = bundle.getPrimary().getCreationTimestamp();
                    sendStatusReportBuffer.add(new StatusReportUtilObject(nodeID, timestamp, FORWARDED));
                }
                BPA.getInstance().saveToQueue(bundle);
            }
        }
    }
}

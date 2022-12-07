package SendStrings;

import BPv7.ApplicationAgent;
import BPv7.containers.NodeID;
import BPv7.interfaces.ApplicationAgentInterface;
import BPv7.interfaces.ApplicationAgentInterface.ReceivePackage;
import Configs.Host;
import Configs.SimulationParams;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.System.*;

/**
 * Main driver of the program, simulations, and statistics
 */
public class App {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(App.class.getName());


    //program argument validation
    /** error message to display when program is not run with correct parameters */
    private static final String errmsg = "Usage: java App [A, Forwarding, B] [0, 1, 2, 3, 4]";
    /** For consistency when exiting */
    private static final int SYSERR = -1;
    /** list of all possible host IDs */
    private static final String[] hostids = {"A", "Forwarding", "B"};//converted to lowercase
    /** list of all possible simulation IDs */
    private static final int[] simulationids = {0, 1, 2, 3, 4};

    //singleton
    /** Simulation parameters singleton copy */
    private static SimulationParams simParams = null;
    /** sync message to be standardized across all hosts*/
    private static byte[] syncMsg = null;

    /**
     * Starts the app!  Loads configurations, starts simulations.
     */
    public static void main(String[] args){
        try {
            InputStream configFile = new FileInputStream(SimulationParams.resourceDir + "logger.properties");
            LogManager.getLogManager().readConfiguration(configFile);
            configFile.close();
        } catch (IOException ex) {
            out.println("WARNING: Could not open configuration file");
            out.println("WARNING: Logging not configured (console output only)");
        }
        logger.info("Starting main app");//first line in logger

        //validate args
        if(args.length != 2){
            err.println(errmsg);
            System.exit(SYSERR);
        }

        //get which host we are from args[0]
        String hostname = args[0].toLowerCase();
        if(Arrays.stream(hostids).noneMatch(h -> h.equalsIgnoreCase(hostname))){
            err.println("Bad host ID (\"" + args[1] + "\"):: " + errmsg);
            System.exit(SYSERR);
        }

        //get which simulation we are running from args[1]
        int simid = -1;
        try {
            simid = Integer.parseInt(args[1]);
            int finalSimid = simid;//streaming below makes me do this and I'm lazy
            if(Arrays.stream(simulationids).noneMatch(id -> id == finalSimid)){
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            err.println("Bad simulation ID (\"" + args[1] + "\"):: " + errmsg);
            System.exit(SYSERR);
        }

        //format of simulation configuration file names:
        // "Sim_[sim number]_[hostname]" (.json done in SimulationParameters)
        String simFile = "Sim_" + simid + "_" + hostname;


        //read simulation parameters
        //will also call SimulationParams.setUpSimulation();
        SimulationParams.setConfigFile(simFile);
        simParams = SimulationParams.getInstance();
        syncMsg = ("Scenario = " + simParams.scenario.toStringShort()).getBytes();

        //set up BPA/entire Bundle protocol tech stack
        ApplicationAgent.getInstance();

        //sync all hosts
        (new App()).doSync();

        //run simulations
        switch(SimulationParams.getInstance().currHost){
            //run multiple simulations here and record stats along the way
            case HOST_A -> (new App()).sendThoseStrings();
            //get ready to receive!
            case HOST_B -> (new App()).receiveThoseStrings();
            //note: HOST_FORWARD just needs BPA to be running, it doesn't receive anything
        }
    }

    /**
     * Does a host sync to ensure logical correctness of simulations
     */
    private void doSync(){
        switch(SimulationParams.getInstance().currHost){
            //run multiple simulations here and record stats along the way
            case HOST_A -> this.sendSync();
            //get ready to receive!
            case HOST_B -> this.receiveSync();
            case HOST_FORWARDING -> this.receiveSync();
        }
    }

    /**
     * If expected != actual, logs and exits
     * @param expected expected message
     * @param actual actual message
     * @param sender sending node
     */
    private boolean checkMsg(byte[] expected, byte[] actual, NodeID sender){
        if (!Arrays.equals(expected, actual)) {//verify SYNC
            logger.severe("Host " + sender.id() + " did not send back good SYNC message: actual: \""
                    + (new String(actual)) + "\", expected: \"" + (new String(expected)) + "\"");
            System.exit(SYSERR);
        }
        return true;
    }

    /**
     * Sends a SYNC message to other hosts to make sure all are running the same simulation.
     * Then, waits for a response from each host as confirmation.
     */
    private void sendSync(){
        logger.info("Sending first sync from host " + simParams.currHost.getName());

        //communicate with other hosts to make sure we are running the same simulation
        // Ideally, this would be done with TCP (i.e. not Bundle), but we are running out of time :/
        ApplicationAgentInterface aa = ApplicationAgent.getInstance();

        //send to B and FORWARDING
        NodeID fNID = new NodeID(Host.HOST_FORWARDING.getName()),
               bNID = new NodeID(Host.HOST_B.getName());
        aa.send(syncMsg, bNID);
        aa.send(syncMsg, fNID);

        //wait for response to exactly match what we sent
        boolean bConfirmed = false, fConfirmed = false;
        try {
            //get the next two bundles
            while(!bConfirmed || !fConfirmed){
                ReceivePackage rp = aa.read(simParams.maxBundleSize);

                if (bNID.equals(rp.sender())) {//check if from HOST_B
                    if(bConfirmed){
                        logger.warning("Received a second SYNC message from B!");
                    }
                    //this function will exit if they aren't equal, so this is gratuitous but whatever
                    bConfirmed = checkMsg(syncMsg, rp.payload(), rp.sender());
                } else if (fNID.equals(rp.sender())) {//check if from HOST_FORWARD
                    if(fConfirmed){
                        logger.warning("Received a second SYNC message from B!");
                    }
                    //this function will exit if they aren't equal, so this is gratuitous but whatever
                    fConfirmed = checkMsg(syncMsg, rp.payload(), rp.sender());//verify SYNC
                } else {//something is very wrong
                    logger.severe("Received message from unknown sender, NodeID = \"" + rp.sender().id() + "\"");
                    System.exit(SYSERR);
                }
            }
        } catch (InterruptedException e) {
            logger.severe("Unable to read next payload from BP!");
            System.exit(SYSERR);
        }
        //either we exited or b and f are confirmed at this point, return to controller
        logger.info("Host " + simParams.currHost.getName() + " finished with SYNC send");
    }

    /**
     * Does the receiving side of the SYNC protocol I invented (receive, verify, reply in kind)
     */
    private void receiveSync(){
        logger.info("Receiving SYNC message on host " + simParams.currHost.getName());

        //sender's NodeID
        NodeID aNID = new NodeID(Host.HOST_A.getName());

        //first receive scenario confirmation
        ApplicationAgentInterface aa = ApplicationAgent.getInstance();
        try {
            //receive bundle
            ReceivePackage rp = aa.read(simParams.maxBundleSize);
            if(rp.sender().equals(aNID)){//check correct sender
                //this function will exit if they aren't equal
                checkMsg(syncMsg, rp.payload(), rp.sender());

                //if success, reply in kind
                aa.send(syncMsg, aNID);

            } else {//something is very wrong
                logger.severe("Received message from unknown sender, NodeID = \"" + rp.sender().id() + "\"");
                System.exit(SYSERR);
            }
        } catch (InterruptedException e) {
            logger.severe("Unable to read next payload (SYNC) from BP!");
            System.exit(SYSERR);
        }
        logger.info("Host " + simParams.currHost.getName() + " finished with SYNC receive");
    }

    /**
     * Receives for one simulation to completion
     */
    private void receiveThoseStrings() {
    }

//    /**
//     * Writes a new general config file out
//     */
//    public void writeSimulationGeneralConfigs(){
//
//    }

    /**
     * Runs one simulation to completion
     */
    public void sendThoseStrings(){
        logger.info("Starting scenario #" + simParams.scenario.toStringShort());

        //todo:: receive response from other two hosts

        //todo:: send strings/simulate and drive network

        //todo:: basic test: just send "strings" to the other side and see if they receive it, then send exact message back

        //todo:: stress test: do max bundle size of 1000 bytes?

    }
}

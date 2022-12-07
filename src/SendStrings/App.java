package SendStrings;

import BPv7.ApplicationAgent;
import BPv7.interfaces.ApplicationAgentInterface;
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

    /** error message to display when program is not run with correct parameters */
    private static final String errmsg = "Usage: java App [A, Forwarding, B] [0, 1, 2, 3, 4]";

    /** list of all possible host IDs */
    private static final String[] hostids = {"A", "Forwarding", "B"};
    /** list of all possible simulation IDs */
    private static final int[] simulationids = {0, 1, 2, 3, 4};

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
            System.exit(-1);
        }

        //get which host we are from args[0]
        String hostname = args[0].toLowerCase();
        if(Arrays.stream(hostids).noneMatch(h -> h.equalsIgnoreCase(hostname))){
            err.println("Bad host ID (\"" + args[1] + "\"):: " + errmsg);
            System.exit(-1);
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
            System.exit(-1);
        }

        //format of simulation configuration file names:
        // "Sim_[sim number]_[hostname]" (.json done in SimulationParameters)
        String simFile = "Sim_" + simid + "_" + hostname;


        //read simulation parameters
        //will also call SimulationParams.setUpSimulation();
        SimulationParams.setConfigFile(simFile);

        //todo:: run multiple simulations here and record stats along the way
        if(SimulationParams.getInstance().currHost == Host.HOST_A){

        }
        (new App()).sendThoseStrings();
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
        SimulationParams params = SimulationParams.getInstance();
        logger.info("Starting scenario #" + params.scenario.toStringShort());

        //todo:: first: communicate with other hosts to make sure we are running the same simulation
        ApplicationAgentInterface aa = ApplicationAgent.getInstance();


        //todo:: send strings/simulate and drive network

    }
}

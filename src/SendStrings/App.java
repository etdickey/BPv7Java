package SendStrings;

import Configs.SimulationParams;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Main driver of the program, simulations, and statistics
 */
public class App {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(App.class.getName());

    /**
     * Starts the app!  Loads configurations, starts simulations.
     */
    public static void main(String[] args){
        try {
            InputStream configFile = new FileInputStream(SimulationParams.resourceDir + "logger.properties");
            LogManager.getLogManager().readConfiguration(configFile);
            configFile.close();
        } catch (IOException ex) {
            System.out.println("WARNING: Could not open configuration file");
            System.out.println("WARNING: Logging not configured (console output only)");
        }
        logger.info("Starting main app");//first line in logger

        //read simulation parameters (may not need to do this here, may just put it in sendThoseStrings)
        SimulationParams.setUpSimulation();

        //todo:: run multiple simulations here and record stats along the way
        (new App()).sendThoseStrings();
    }

    /**
     * Runs one simulation to completion
     */
    public void sendThoseStrings(){
        SimulationParams params = SimulationParams.getInstance();
        logger.info("Starting scenario #" + params.scenario.toStringShort());
        //todo:: send strings/simulate and drive network
    }
}

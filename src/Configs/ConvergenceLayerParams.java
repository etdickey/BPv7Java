package Configs;

import java.security.InvalidParameterException;
import java.util.logging.Logger;

/**
 * Contains simulation-level parameters only applicable to the convergence layer (DTCP)
 * Examples include which port DTCP receives on and which IP our host/other hosts are.
 */
public class ConvergenceLayerParams {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(ConvergenceLayerParams.class.getName());

    /**
     * config file name
     */
    private static final String CONFIG_FILE =
            SimulationParams.resourceDir + "ConvergenceLayerConfigs" + SimulationParams.cfgFileExtension;

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static ConvergenceLayerParams instance = null;

    //actual class variables
    /**
     * Which port DTCP listens on
     */
    public final int DTCP_Port;
    /**
     * below 1024 are reserved, ports only go to 2^16-1
     */
    private final int MIN_PORT = 1024, MAX_PORT = 1 << 16;


    /**
     * Gets the singleton instance of the convergence layer parameters
     * @return a reference to the convergence layer parameters instance
     * @implNote not making this.instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static ConvergenceLayerParams getInstance(){
        //SimulationParams must be set up first
        SimulationParams.getInstance();
        //Now do self
        if(instance == null){
            synchronized (ConvergenceLayerParams.class){
                if(instance == null){
                    instance = readConvergenceLayerConfigFile();
                    logger.info("Created ConvergenceLayerParams singleton");
                }
            }
        }
        return instance;
    }


    /**
     * Reads in the simulation parameters from the general config file and constructs this class based on that
     * @return instance of this class with correct simulation parameters
     * @throws InvalidParameterException if bad config file(s)
     */
    private static ConvergenceLayerParams readConvergenceLayerConfigFile() throws InvalidParameterException {
        //todo:: read config file in
        // note: saved in CONFIG_FILE variable at top of class
        // note: which host we are stored in SimulationParams.CURR_HOST
        // note: which scenario we are running is stored in SimulationParams.SCENARIO_NUM

        ConvergenceLayerParams ret;
        try{
            ret = new ConvergenceLayerParams(0);
        } catch(InvalidParameterException e){
            logger.severe("ERROR! Unable to parse config files for ConvergenceLayerParams: " + e.getMessage());
            throw new InvalidParameterException(e.getMessage());
        }
        //otherwise, ready to go!
        logger.info("Parsed config file info");
        return ret;
    }

    /**
     * Constructs global convergence layer parameters class based on parameters
     * @param dtcpPort ID of host that we are
     * @throws InvalidParameterException if invalid port num
     */
    protected ConvergenceLayerParams(int dtcpPort) throws InvalidParameterException {
        if(dtcpPort < MIN_PORT || dtcpPort > MAX_PORT) {
            throw new InvalidParameterException("Invalid port num: " + dtcpPort
                    + ", requirements: " + MIN_PORT + " < port < " + MAX_PORT);
        }
        DTCP_Port = dtcpPort;
    }
}

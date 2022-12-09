package Configs;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.logging.Logger;

/**
 * Contains all global parameters relevant to the running of the BP protocol
 * Examples include which host to start up (host A, host B, forwarding host)
 * and log files.
 */
public class SimulationParams {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(SimulationParams.class.getName());

    /**
     * Folder containing config files
     */
    public static final String resourceDir = "./src/Configs/resources/";//testing
//	static String resourceTarget = "";//jar running

    /**
     * Because I might change it to json later
     */
    public static final String cfgFileExtension = ".cfg";

    /**
     * config file name
     * @implNote MIGHT NEED TO CHANGE THIS @ethan
     */
    private static final String CONFIG_FILE = resourceDir + "GeneralConfigs" + cfgFileExtension;
    public static boolean lifetimeExpiredAction;
    public static boolean overUnidirectionalAction;
    public static boolean transmissionCancelledAction;
    public static boolean depletedStorageAction;
    public static boolean destinationUnavailableAction;
    public static boolean noKnownRouteToDestinationAction;
    public static boolean noTimelyContactAction;
    public static boolean blockUnintelligibleAction;
    public static boolean hopLimitExceededAction;
    public static boolean trafficParedAction;
    public static boolean blockUnsupportedAction;

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static SimulationParams instance = null;


    //actual class variables
    /**
     * Which host we are as ID
     */
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    public final int hostID;

    /**
     * Which host we are as Enum
     */
    @JsonIgnore
    public final Host currHost;
    /**
     * Maximum size of a bundle when sending from this specific host
     */
    public final int maxBundleSize;
    /**
     * The actual scenario instance with useful variables
     */
    public final Scenario scenario;
    //todo:: add bundle status report switches?
    //todo:: define transmission timeout (how long to try to send a bundle, right now = 1min)


    //functions
    /**
     * Sets up all global parameters/reads in all relevant config files
     */
    public static void setUpSimulation(){
        SimulationParams.getInstance();
        ConvergenceLayerParams.getInstance();
    }

    /**
     * Gets the singleton instance of the simulation parameters, also sets up convergence layer parameters
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this.instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static SimulationParams getInstance(){
        //noinspection DoubleCheckedLocking
        if(instance == null){
            synchronized (SimulationParams.class){
                if(instance == null){
                    instance = readGeneralConfigFile(CONFIG_FILE);
                    logger.info("Created SimulationParameters singleton");
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
    private static SimulationParams readGeneralConfigFile(@SuppressWarnings("SameParameterValue") String fileName) throws InvalidParameterException {
        //todo:: read config file and determine which host we are
        // note: saved in CONFIG_FILE variable at top of class
        //todo:: get scenario num
        //todo:: read specific scenario file

        SimulationParams ret;
        try{
            ret = new ObjectMapper().readValue(new File(fileName), SimulationParams.class);
        } catch (InvalidParameterException e){
            logger.severe("ERROR! Unable to parse config files for global parameters (SimulationParams): " + e.getMessage());
            throw new InvalidParameterException(e.getMessage());
        } catch (Exception e){
            logger.severe("ERROR! Unable to parse config files for ConvergenceLayerParams: " + e.getMessage());
            throw new InvalidParameterException(e.getMessage());
        }
        //otherwise, ready to go!
        logger.info("Parsed config file info");
        return ret;
    }

    /**
     * Constructs global simulation parameters class based on parameters
     * @param hostID ID of host that we are
     * @param scenario scenario we are running
     * @param maxBundleSize maximum bundle size allowed in the convergence layer of this host
     * @throws InvalidParameterException if invalid hostID or scenario num
     */
    @JsonCreator
    public SimulationParams(@JsonProperty("hostID") int hostID,
                            @JsonProperty("scenario") Scenario scenario,
                            @JsonProperty("maxBundleSize") int maxBundleSize) throws InvalidParameterException {
        this.hostID = hostID;
        this.currHost = Host.getHost(hostID);
        this.scenario = scenario;//todo:: validate scenario num (likely need an enum like Host)
        this.maxBundleSize = maxBundleSize;
    }
}

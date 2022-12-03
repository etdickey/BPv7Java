package Configs;

import java.security.InvalidParameterException;
import java.util.Map;
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("FieldCanBeLocal")
    private final int MIN_PORT = 1024, MAX_PORT = 1 << 16;

    /**
     * The probability of a link having an expected disconnect/ going "down".
     *  Both sides of a link are aware of this down.
     */
    public int expectedDownProbability;

    /**
     * The probability of a link having an unexpected disconnect/ going "down".
     * Neither side is aware of this, other than dropping all bundles during the time frame this occurs during
     */
    public int unexpectedDownProbability;

    /**
     * The number of milliseconds per Timeframe, essentially how many milliseconds between occurrences and length of
     * an expected and unexpected down (multiple can occur back to back).
     */
    public int milliPerDownPeriod;

    /**
     * The Routing Map for the current node, specified in context file, of the form URI/Node ID String -> IPv4 Address
     */
    public Map<String, String> idToAddressRoutingMap;

    /**
     * The IP address of this node, when sending to yourself.
     * Addtionally, a routing to this address in the routing table
     * is interpreted as no route, but ID exists
     * (Similar to a DNS response but cannot reach IP address)
     */
    public String thisAddress;

    /**
     * The capacity of the internal receive queue. -1 for no limit.
     */
    public int queueCapacity; //-1 for no limit

    /**
     * The number of threads in the receiving thread pool
     */
    public int nThreads;

    /**
     * Max number of connections to have at once
     */
    public int maxConnections;

    /**
     * The connection timeout for the read socket for connections from clients
     */
    public int connectionTimeout;

    /**
     * How long to wait for a spot in the receive queue before dropping a bundle
     */
    public int queueTimeoutInMillis;


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
        //noinspection DoubleCheckedLocking
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
            // Insert Reading Here!
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

package Configs;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

/**
 * Contains simulation-level parameters only applicable to the convergence layer (DTCP)
 * Examples include which port DTCP receives on and which IP our host/other hosts are.
 */


public class ConvergenceLayerParams {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(ConvergenceLayerParams.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static ConvergenceLayerParams instance = null;

    /** The config file this was read from */
    private static String CONFIG_FILE = null;

    /** Whether the Config File has been set or not */
    private static boolean hasSetConfig = false;

    /**
     * The method for setting the ConvergenceLayerParams config file
     * @param file the config file for this instance
     */
    @SuppressWarnings("unused")
    public static void setConfigFile(String file) {
        if (!hasSetConfig) {
            hasSetConfig = true;
            CONFIG_FILE = file;
            getInstance();
        }
    }

    // Parameter Checking Variables
    /**
     * below 1024 are reserved, ports only go to 2^16-1
     */
    @SuppressWarnings("FieldCanBeLocal")
    private static final int MIN_PORT = 1024, MAX_PORT = 1 << 16;



    //actual class variables
    /**
     * The probability of a link having an expected disconnect/ going "down".
     *  Both sides of a link are aware of this down.
     */
    public final double expectedDownProbability;

    /**
     * The probability of a link having an unexpected disconnect/ going "down".
     * Neither side is aware of this, other than dropping all bundles during the time frame this occurs during
     */
    public final double unexpectedDownProbability;

    /**
     * The number of milliseconds per Timeframe, essentially how many milliseconds between occurrences and length of
     * an expected and unexpected down (multiple can occur back to back).
     */
    public final int milliPerDownPeriod;

    /**
     * The Routing Map for the current node, specified in context file, of the form URI/Node ID String -> IPv4 Address
     * May map to null if host exists but no known route
     */
    public final Map<String, String> idToAddressRoutingMap;

    /**
     * The IP address of this node, when sending to yourself.
     */
    public final String thisAddress;

    /**
     * Which port DTCP listens on
     */
    public final int dtcpPort;

    /**
     * The capacity of the internal receive queue. -1 for no limit.
     */
    public final int queueCapacity; //-1 for no limit

    /**
     * The number of threads in the receiving thread pool
     */
    public final int nThreads;

    /**
     * Max number of connections to have at once
     */
    public final int maxConnections;

    /**
     * The connection timeout for the read socket for connections from clients
     */
    public final int connectionTimeout;

    /**
     * How long to wait for a spot in the reception queue before dropping a bundle
     */
    public final int queueTimeoutInMillis;


    /**
     * Gets the singleton instance of the convergence layer parameters
     * @return a reference to the convergence layer parameters instance
     * @implNote not making this.instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static ConvergenceLayerParams getInstance(){
        //SimulationParams must be set up first
        //SimulationParams.getInstance();
        //Now do self
        //noinspection DoubleCheckedLocking
        if(instance == null){
            synchronized (ConvergenceLayerParams.class){
                if(instance == null){
                    if (hasSetConfig)
                        instance = readConvergenceLayerConfigFile(CONFIG_FILE);
                    else
                        instance = readConvergenceLayerConfigFile();
                    logger.info("Created ConvergenceLayerParams singleton");
                }
            }
        }
        return instance;
    }


    /**
     * This is a default "reader" for the simulation parameters from the general config file and constructs this
     * class based on default values. Should probably not be called, but useful on the backend
     * @return instance of this class with correct simulation parameters
     * @throws InvalidParameterException if bad config file(s)
     */
    private static ConvergenceLayerParams readConvergenceLayerConfigFile() throws InvalidParameterException {
        ConvergenceLayerParams ret;
        try {
            Map<String, String> defaultRoutingTable = new HashMap<>();
            defaultRoutingTable.put("localhost", "127.0.0.1");
            ret = new ConvergenceLayerParams(0.1f, 0.01f, 100,
                        defaultRoutingTable,"127.0.0.1", 3827, -1, 10,
                        100, 100, 100);
        } catch(InvalidParameterException e){
            logger.severe("ERROR! Unable to create default for ConvergenceLayerParams: " + e.getMessage());
            throw new InvalidParameterException(e.getMessage());
        }
        //otherwise, ready to go!
        return ret;
    }

    /**
     * This reads
     * @param file the configuration file to read in
     * @return instance of this class with correct simulation parameters
     * @throws InvalidParameterException One of the parameters is incorrect
     */
    private static ConvergenceLayerParams readConvergenceLayerConfigFile(String file) throws InvalidParameterException {
        ConvergenceLayerParams ret;
        try {
            ret = new ObjectMapper().readValue(new File(file), ConvergenceLayerParams.class);
        } catch(Exception e){
            logger.severe("ERROR! Unable to parse config files for ConvergenceLayerParams: " + e.getMessage());
            throw new InvalidParameterException(e.getMessage());
        }
        //otherwise, ready to go!
        logger.info("Parsed config file info");
        return ret;
    }

    /**
     * Constructs global convergence layer parameters class based on parameters
     * @param expectedDownProbability The probability of a link having an expected disconnect/ going "down".
     * @param unexpectedDownProbability The probability of a link having an unexpected disconnect/ going "down".
     * @param milliPerDownPeriod The number of milliseconds per Timeframe
     * @param idToAddressRoutingMap The Routing Map for the current node
     * @param thisAddress The IP address of this node, when sending to yourself
     * @param dtcpPort Which port DTCP listens on, (use 3827 by default)
     * @param queueCapacity The capacity of the internal receive queue. -1 for no limit
     * @param nThreads The number of threads in the receiving thread pool
     * @param maxConnections Max number of connections to have at once
     * @param connectionTimeout The connection timeout for the read socket for connections from clients
     * @param queueTimeoutInMillis How long to wait for a spot in the receive queue before dropping a bundle
     * @throws InvalidParameterException if invalid parameters
     */
    @JsonCreator
    public ConvergenceLayerParams(@JsonProperty("expectedDownProbability") double expectedDownProbability,
                                  @JsonProperty("unexpectedDownProbability") double unexpectedDownProbability,
                                  @JsonProperty("milliPerDownPeriod") int milliPerDownPeriod,
                                  @JsonProperty("idToAddressRoutingMap") Map<String, String> idToAddressRoutingMap,
                                  @JsonProperty("thisAddress") String thisAddress,
                                  @JsonProperty("dtcpPort") int dtcpPort,
                                  @JsonProperty("queueCapacity") int queueCapacity,
                                  @JsonProperty("nThreads") int nThreads,
                                  @JsonProperty("maxConnections") int maxConnections,
                                  @JsonProperty("connectionTimeout") int connectionTimeout,
                                  @JsonProperty("queueTimeoutInMillis") int queueTimeoutInMillis) throws InvalidParameterException {
        // Check Values
        if (expectedDownProbability < 0 || expectedDownProbability >= 1)
            throw new InvalidParameterException("Invalid expectedDownProbability: " + expectedDownProbability
                    + ", must be in [0,1).");
        if (unexpectedDownProbability < 0 || unexpectedDownProbability >= 1)
            throw new InvalidParameterException("Invalid unexpectedDownProbability: " + unexpectedDownProbability
                    + ", must be in [0,1).");
        if (milliPerDownPeriod <= 0)
            throw new InvalidParameterException("Invalid milliPerDownPeriod: " + milliPerDownPeriod + " must be > 0");
        if (idToAddressRoutingMap == null || idToAddressRoutingMap.size() == 0)
            throw new InvalidParameterException("Invalid idToAddressRoutingMap, null or empty: " + milliPerDownPeriod);
        if (thisAddress == null)
            throw new InvalidParameterException("Invalid thisAddress, null");
        if (dtcpPort < MIN_PORT || dtcpPort > MAX_PORT)
            throw new InvalidParameterException("Invalid port num: " + dtcpPort
                    + ", requirements: " + MIN_PORT + " < port < " + MAX_PORT);
        if (queueCapacity < -1 || queueCapacity == 0)
            throw new InvalidParameterException("Invalid queue capacity: " + queueCapacity + ", must be (strictly) positive or -1");
        if (nThreads <= 0)
            throw new InvalidParameterException("Invalid nThreads: " + nThreads + ", must be (strictly) positive");
        if (maxConnections <= 0)
            throw new InvalidParameterException("Invalid maxConnections: " + maxConnections + ", must be (strictly) positive");
        if (connectionTimeout <= 0)
            throw new InvalidParameterException("Invalid connectionTimeout: " + connectionTimeout + ", must be (strictly) positive");
        if (queueTimeoutInMillis <= 0)
            throw new InvalidParameterException("Invalid queueTimeoutInMillis: " + queueTimeoutInMillis + ", must be (strictly) positive");

        // Set the values
        this.dtcpPort = dtcpPort;
        this.expectedDownProbability = expectedDownProbability;
        this.unexpectedDownProbability = unexpectedDownProbability;
        this.milliPerDownPeriod = milliPerDownPeriod;
        this.idToAddressRoutingMap = idToAddressRoutingMap;
        this.thisAddress = thisAddress;
        this.queueCapacity = queueCapacity;
        this.nThreads = nThreads;
        this.maxConnections = maxConnections;
        this.connectionTimeout = connectionTimeout;
        this.queueTimeoutInMillis = queueTimeoutInMillis;
    }
}

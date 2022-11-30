package DTCP;

import BPv7.containers.Bundle;
import Configs.ConvergenceLayerParams;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a utility class for all the things called in multiple parts of DTCP that don't necessarily need their own class
 */
class DTCPUtils {

    /**
     * The instance of the ConvergenceLayerParams Class
     */
    private static final ConvergenceLayerParams config = ConvergenceLayerParams.getInstance();

    /**
     * The latest timeFrame where a down check occured. Basically the last timeframe where the connectionDownTimeFrame
     * was updated
     */
    private static Long lastTimeFrame;

    /**
     * A lock for messing with timeFrame stuff, since could be happening by several threads
     */
    private static final Object timeFrameLock = new Object();

    /**
     * The map of the current connectionDownTimeFrame values. To make sure that things aren't calculated more than once
     * and to speed up some calculations (hopefully)
     */
    private static Map<Long, Double> connectionDownTimeFrame = new HashMap<>();


    /**
     * Logger for this class. Prepends all logs from this class with the class name
     * Might not be needed, but better to be safe than sorry
     */
     static final Logger logger = Logger.getLogger(DTCPUtils.class.getName());

    /**
     * Gets the current timeframe, aka the number of milliseconds between occurrences and length of
     * an expected and unexpected down (multiple can occur back to back), and how often a connection
     * needs to be re-checked. There can be connections that last over this period if they start before hand,
     * but this is equivalent to saying the sender is responsible for ensuring ann expected down doesn't occur
     * before its received (ex: space networks)
     * @return Get the current rounded time frame for the current connection
     */
    private static long getCurrentTimeFrame() {
        Instant now = Instant.now();
        return now.toEpochMilli() % config.milliPerDownPeriod;
    }


    /**
     * Gets the corresponding value for
     * @param connectionID the connection ID to calculate this for (used in seeding along with timeframe)
     * @return the double representing where on [0,1) the current time frame check landed on
     */
    private static double getConnectionValue(long connectionID) {
        long timeFrame = getCurrentTimeFrame();
        double result;
        synchronized (timeFrameLock) {
            if (lastTimeFrame == timeFrame && connectionDownTimeFrame.containsKey(connectionID)) {
                result = connectionDownTimeFrame.get(connectionID);
            }
            else {
                if (lastTimeFrame < timeFrame) {
                    lastTimeFrame = timeFrame;
                    connectionDownTimeFrame = new HashMap<>();
                }
                // result is the double representing where on [0,1) the current time frame check landed on
                result = (new Random(connectionID ^ timeFrame)).nextDouble();
                connectionDownTimeFrame.put(connectionID, result);
                logger.log(Level.INFO, "New Connection Check For Connection Id: " + connectionID + "Value: " + result);
            }
        }
        return result;
    }

    /**
     * Get if the connection to the destination address after routing is expected to be down
     * @param destAddress the address of the next hop
     * @return true if the connection is expected to be down, otherwise false
     */
    @SuppressWarnings("DuplicatedCode")
    public static boolean isConnectionDownExpected(String destAddress) {
        long firstAddr = addressToLong(config.thisAddress);
        long secondAddr = addressToLong(destAddress);
        if (secondAddr == firstAddr)
            return false;
        if (firstAddr > secondAddr) {
            // This swaps without another variable, and is potentially faster
            firstAddr ^= secondAddr;
            secondAddr ^= firstAddr;
            firstAddr ^= secondAddr;
        }
        long connectionID = (secondAddr << 16 + firstAddr);
        return getConnectionValue(connectionID) < config.expectedDownProbability;
    }

    /**
     * Get if the bundles on a specific connection should be dropped or not (unexpected down)
     * @param srcAddress the address of the previous hop
     * @return true if the bundle should be dropped
     */
    @SuppressWarnings("DuplicatedCode")
    public static boolean isConnectionDownUnexpected(String srcAddress) {
        long firstAddr = addressToLong(config.thisAddress);
        long secondAddr = addressToLong(srcAddress);
        if (secondAddr == firstAddr)
            return false;
        if (firstAddr < secondAddr) {
            firstAddr ^= secondAddr;
            secondAddr ^= firstAddr;
            firstAddr ^= secondAddr;
        }
        long connectionID = (secondAddr << 16 + firstAddr);
        return getConnectionValue(connectionID) < config.unexpectedDownProbability;
    }

    /**
     * Convert a String IP Address to a long. Used for Randomization
     * @param address the IP address to convert
     * @return the long version of the ip address as an int.
     */
    public static long addressToLong(String address) {
        String[] part = address.split("\\.");
        long num = 0;
        for (String bt : part) {
            num <<= 8;
            num += Integer.parseInt(bt) % 256;
        }
        return num;
    }

    /**
     * Generates a bundle logging id for the given bundle, of the form:
     * [SRC NODE ID]:[CREATION TIMESTAMP in MS]:[SEQ NUMBER]
     * @param bundle the bundle the ID is being generated for
     * @return the bundle logging ID
     */
    public static String getLoggingBundleId(Bundle bundle) {
        return bundle.getPrimary().getSrcNode().id()
                + ':' + bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS()
                + ':' + bundle.getPrimary().getCreationTimestamp().getSeqNum();
    }


}

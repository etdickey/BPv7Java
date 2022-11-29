package DTCP;

import BPv7.containers.Bundle;
import Configs.ConvergenceLayerParams;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class DTCPUtils {

    /**
     * The instance of the ConvergenceLayerParams Class
     */
    private static final ConvergenceLayerParams config = ConvergenceLayerParams.getInstance();

    private static Long lastTimeFrame;
    private static final Object timeFrameLock = new Object();
    private static HashMap<Long, Integer> connectionDownTimeFrame = new HashMap<>();


    /**
     * Logger for this class. Prepends all logs from this class with the class name
     * Might not be needed, but better to be safe than sorry
     */
     static final Logger logger = Logger.getLogger(DTCPUtils.class.getName());

    /**
     * Gets the current rounded timeframe
     * @return Get the current rounded time frame for the current connection
     */
    private static long getCurrentTimeFrame() {
        Instant now = Instant.now();
        long timeframe = 0;
        timeframe += now.getEpochSecond() * 1000;
        timeframe += ((now.toEpochMilli() % 1000) / (config.milliPerDownPeriod)) * config.milliPerDownPeriod;
        return timeframe;
    }


    private static int getConnectionValue(long connectionID, boolean expected) {
        long timeFrame = getCurrentTimeFrame();
        int result;
        synchronized (timeFrameLock) {
            if (lastTimeFrame == timeFrame && connectionDownTimeFrame.containsKey(connectionID)) {
                result = connectionDownTimeFrame.get(connectionID);
            }
            else {
                if (lastTimeFrame != timeFrame) {
                    lastTimeFrame = timeFrame;
                    connectionDownTimeFrame = new HashMap<>();
                }
                result = (new Random(connectionID ^ timeFrame)).nextInt(config.totalProbabilty);
                connectionDownTimeFrame.put(connectionID, result);
                logger.log(Level.INFO, "New Connection Check For Connection Id (expected: " + expected + "): " + connectionID + "Value: " + result);
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
            firstAddr ^= secondAddr;
            secondAddr ^= firstAddr;
            firstAddr ^= secondAddr;
        }
        long connectionID = (secondAddr << 16 + firstAddr);
        return getConnectionValue(connectionID, true) < config.expectedDownProbability;
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
        return getConnectionValue(connectionID, false) < config.unexpectedDownProbability;
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

    public static String getLoggingBundleId(Bundle bundle) {
        return bundle.getPrimary().getSrcNode().id()
                + ':' + bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS()
                + ':' + bundle.getPrimary().getCreationTimestamp().getSeqNum();
    }


}

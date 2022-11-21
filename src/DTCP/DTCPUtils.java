package DTCP;

import BPv7.containers.NodeID;
import Configs.ConvergenceLayerParams;

import java.time.Instant;
import java.util.Random;
import java.util.logging.Logger;

class DTCPUtils {

    /**
     * The instance of the ConvergenceLayerParams Class
     */
    private static final ConvergenceLayerParams config = ConvergenceLayerParams.getInstance();


    /**
     * Logger for this class. Prepends all logs from this class with the class name
     * Might not be needed, but better to be safe than sorry
     */
     static final Logger logger = Logger.getLogger(DTCPUtils.class.getName());

    /**
     *
     * @param ID the URI of the destination
     * @return The IP address of the destination node
     */
    public static String nodeToString(NodeID ID) {
        //TODO: Implement This
        return "";
    }

    /**
     * Gets the current rounded timeframe
     * @return Get the current rounded time frame for the current connection
     */
    public static long getCurrentTimeFrame() {
        Instant now = Instant.now();
        long timeframe = 0;
        timeframe += now.getEpochSecond() * 1000;
        timeframe += ((now.toEpochMilli() % 1000) / (config.milliPerDownPeriod)) * config.milliPerDownPeriod;
        return timeframe;
    }

    /**
     * Get if the connection to the destination address after routing is expected to be down
     * @param destAddress the address of the next hop
     * @return true if the connection is expected to be down, otherwise false
     */
    @SuppressWarnings("DuplicatedCode")
    public static boolean isConnectionDownExpected(String destAddress) {
        long thisAddr = addressToLong(config.thisAddress);
        long destAddr = addressToLong(destAddress);
        if (destAddr == thisAddr)
            return false;
        long seed;
        seed = (destAddr << 16 + thisAddr) ^ getCurrentTimeFrame();
        Random generator = new Random(seed);
        int res = generator.nextInt(config.totalChance);
        return res < config.expectedDownChance;
    }

    /**
     * Get if the bundles on a specific connection should be dropped or not (unexpected down)
     * @param srcAddress the address of the previous hop
     * @return true if the bundle should be dropped
     */
    @SuppressWarnings("DuplicatedCode")
    public static boolean isConnectionDownUnexpected(String srcAddress) {
        long thisAddr = addressToLong(config.thisAddress);
        long srcAddr = addressToLong(srcAddress);
        if (srcAddr == thisAddr)
            return false;
        long seed;
        seed = (thisAddr << 16 + srcAddr) ^ getCurrentTimeFrame();
        Random generator = new Random(seed);
        int res = generator.nextInt(config.totalChance);
        return res < config.unexpectedDownChance;
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


}

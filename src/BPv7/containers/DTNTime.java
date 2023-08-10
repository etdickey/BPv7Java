package BPv7.containers;

import java.util.Objects;

/**
 * A DTN time is an unsigned integer indicating the number of milliseconds
 * that have elapsed since the DTN Epoch, 2000-01-01 00:00:00 +0000 (UTC).
 * DTN time is not affected by leap seconds.
 * "Implementers need to be aware that DTN time values conveyed in CBOR
 *  encoding in bundles will nearly always exceed (232 - 1); the manner
 *  in which a DTN time value is represented in memory is an
 *  implementation matter."
 *
 * @implSpec CBOR unsigned int
 */
public class DTNTime {
    /**
     * Number of milliseconds from EPOCH to DTN EPOCH
     */
    private static final long DTNEpoc = 946702800000L;

    /**
     * MS since the DTN Epoch, 2000-01-01 00:00:00 +0000 (UTC)
     * @implNote We choose to handle this by modding by 2^32 because
     *  we assume a bundle won't be stalled for more than 49 days
     */
    public final int timeInMS;

    /**
     * Calculates the current DTNTime (ms since DTN Epoch)
     * @return current DTNTime (ms since DTN Epoch)
     */
    private static long getDeltaTime(){ return System.currentTimeMillis() - DTNEpoc; }

    /**
     * "The DTN time value zero indicates that the time is unknown"
     * @return a value representing an unknown DTNTime
     */
    public static DTNTime getUnknownDTNTime(){ return new DTNTime(0); }

    /**
     * Calculates the current DTNTime and returns it wrapped in this class
     * @return current DTNTime
     */
    public static DTNTime getCurrentDTNTime(){ return new DTNTime(); }

    /**
     * Constructs a DTNTime initialized to the current time
     */
    protected DTNTime(){
        //choosing to handle it by % 2^31-1 (max signed int)
        timeInMS = ((int) (getDeltaTime() % (1L << 31)));
    }

    /**
     * Constructs a DTNTime initialized to the given time
     * @param t time to initialize to
     */
    protected DTNTime(int t){ timeInMS = t; }

    public int getTimeInMS() {
        return timeInMS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTNTime dtnTime)) return false;
        return timeInMS == dtnTime.timeInMS;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeInMS);
    }
}

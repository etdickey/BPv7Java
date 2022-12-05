package BPv7.containers;

/**
 * Wraps the timestamp of a bundle (two ints)
 *
 * @implSpec CBOR array
 */
public class Timestamp {

    public static final Timestamp UNKNOWN_TIMESTAMP = getUnknownTimestamp();
    /**
     * Bundle creation time in ms since EPOC
     * CBOR:: check DTNTime header
     */
    public DTNTime creationTime;
    /**
     * Sequence number -- to add greater than ms granularity (for bundles created in the same ms)
     * @implSpec CBOR unsigned int
     */
    public int seqNum;

    /**
     * Default constructor
     *
     * @param creationTime creation time in ms
     * @param seqNum       sequence num
     */
    public Timestamp(DTNTime creationTime, int seqNum) {
        this.creationTime = creationTime;
        this.seqNum = seqNum;
    }

    /**
     * The DTN time value zero indicates that the time is unknown
     * @return a value representing an unknown Timestamp
     */
    private static Timestamp getUnknownTimestamp() {
        return new Timestamp(DTNTime.getUnknownDTNTime(), -1);
    }

    //getters
    public DTNTime getCreationTime() {
        return creationTime;
    }

    public int getSeqNum() {
        return seqNum;
    }

    //setters
    public void setCreationTime(DTNTime creationTime) {
        this.creationTime = creationTime;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }
}

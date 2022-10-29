package BPv7.containers;

/**
 * Wraps the timestamp of a bundle (two ints)
 * @implSpec CBOR array
 */
public class Timestamp {
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
     * @param creationTime creation time in ms
     * @param seqNum sequence num
     */
    public Timestamp(DTNTime creationTime, int seqNum) {
        this.creationTime = creationTime;
        this.seqNum = seqNum;
    }
}

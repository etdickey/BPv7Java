package BPv7.containers;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Wraps the timestamp of a bundle (two ints)
 *
 * @param creationTime Bundle creation time in ms since EPOC; CBOR:: check DTNTime header
 * @param seqNum Sequence number -- to add greater than ms granularity (for bundles created in the same ms); CBOR unsigned int
 * @implSpec CBOR array
 */
public record Timestamp(DTNTime creationTime, int seqNum) {
    public static final Timestamp UNKNOWN_TIMESTAMP = getUnknownTimestamp();

    /**
     * The DTN time value zero indicates that the time is unknown
     * @return a value representing an unknown Timestamp
     */
    @JsonIgnore
    private static Timestamp getUnknownTimestamp() {
        return new Timestamp(DTNTime.getUnknownDTNTime(), -1);
    }
}

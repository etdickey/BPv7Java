package BPv7.containers;

/**
 * Represents the PayloadBlock
 */
public class PayloadBlock extends CanonicalBlock{
    /** Block types for this type of block */
    private static final int PAYLOAD_BLOCK_TYPE = 1, PAYLOAD_BLOCK_NUM = 1;
    /** What this block contains */
    protected byte[] payload;

    /** Default constructor */
    public PayloadBlock(byte[] payload) {
        super(PAYLOAD_BLOCK_TYPE, PAYLOAD_BLOCK_NUM);
        this.payload = payload;
    }

    //getters and setters
    public byte[] getPayload() { return payload; }
    public void setPayload(byte[] payload) { this.payload = payload; }
}

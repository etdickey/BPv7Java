package BPv7.containers;

/**
 * Represents the PayloadBlock
 */
public class PayloadBlock extends CanonicalBlock{
    /**
     * Block types for this type of block
     */
    private static final int PAYLOAD_BLOCK_TYPE = 1, PAYLOAD_BLOCK_NUM = 1;
    /**
     * What this block contains
     */
    protected String payload;

    /**
     * Default constructor
     */
    public PayloadBlock(String payload) {
        super(PAYLOAD_BLOCK_TYPE, PAYLOAD_BLOCK_NUM);
        this.payload = payload;
    }

    //getters and setters
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}

package BPv7.containers;

import javax.naming.directory.InvalidAttributesException;
import java.util.InvalidPropertiesFormatException;

/**
 * Represents every block except the first containing who knows what.
 * @implSpec CBOR array with length:<br>
 *      5: if the block has no CRC (type=0)<br>
 *      6: if the block has a CRC (type!=0)<br>
 */
public abstract class CanonicalBlock extends Block {
    /**
     * Flag bitmasks
     */
    private final int   REPL = 0x1,//1 = block must be replicated in every fragment
                        TMFR = 0x2,//1 = transmit status report if block fails to be processed
                        FDBU = 0x4,//1 = delete bundle if block fails to be processed
                        FDBL = 0x10;//1 = discard block if block fails to be processed
    /**
     * Block type code
     * 0 = Reserved<br>
     * 1 = Bundle Payload Block<br>
     * 6 = Previous node (proximate sender)<br>
     * 7 = Bundle age (ms)<br>
     * 10 = Hop count (#prior transmit (xmit) attempts)<br>
     * 11-191 = reserved for private/experimental use<br>
     * 192-255 = not reserved, free for experimentation
     * All other code values are reserved for future use
     *
     * @implSpec CBOR unsigned int
     */
    protected final int blockType;
    /**
     * Unique ID in the bundle
     * @implSpec CBOR unsigned int
     */
    protected final int blockNum;
    /**
     * Block processing control flags
     *
     * Bits:
     * 0-6 reserved
     * 7-63 unassigned
     *
     * @implSpec CBOR unsigned 64 bit int
     * @implNote every other reserved bit's value must be ignored
     *  (when parsing, don't throw an error if it's 1)
     */
    protected long flags;
    /**
     * CRC type, valid values:<br>
     * 0 indicates "no Cyclic Redundancy Check (CRC) is present."<br>
     * 1 indicates "a standard X-25 CRC-16 is present." [CRC16]<br>
     * 2 indicates "a standard CRC32C (Castagnoli) CRC-32 is present." [RFC4960]<br>
     *
     * @implSpec CBOR unsigned int
     * @implNote We didn't do this... But if you feel like
     *   implementing this: "For examples of CRC32C CRCs, see
     *   Appendix A.4 of [RFC7143]"
     */
    protected final int crcType = GLOBAL_CRC_TYPE;
    /**
     * CRC value (like a checksum, but it's a hash!)
     *
     * @implSpec CBOR ?? -- depends on CRCType. We use a 2-byte crc:: unsigned 16 bit int<br>
     *  Allowed to not be present if bundle has a BPSEC block.<br>
     *  "The CRC SHALL be computed over the concatenation of all bytes
     *  of the block (including CBOR "break" characters) including the
     *  CRC field itself, which, for this purpose, SHALL be temporarily
     *  populated with all bytes set to zero."
     * @implNote <a href="https://crccalc.com/">CRC calculator</a>
     */
    protected short crc;//4 bytes for CRC-16/X-25, which we hardcoded above
    //payload here, but it changes per block type

    /**
     * Primary constructor with required fields
     * @param blockType type of block
     * @param blockNum unique identifier within the bundle
     */
    public CanonicalBlock(int blockType, int blockNum) {
        this.flags = 0;
        this.blockType = blockType;
        this.blockNum = blockNum;
        this.crc = -1;
    }

    //getters
    public int getBlockType() { return blockType; }
    public int getBlockNum() { return blockNum; }
    public long getFlags() { return flags; }
    public int getCrcType() { return crcType; }
    public int getCrc() { return crc; }

    //setters
    public void setCrc(short crc) { this.crc = crc; }

    public void setREPL() { this.flags = flags | REPL;/*sets the REPL flag*/ }
    public void setTMFR() { this.flags = flags | TMFR;/*sets the TMFR flag*/ }
    public void setFDBU() { this.flags = flags | FDBU;/*sets the FDBU flag*/ }
    public void setFDBL() { this.flags = flags | FDBL;/*sets the FDBL flag*/ }
    //unsetters :)
    private void unsetREPL() { this.flags = flags & (~REPL);/*unsets the REPL flag*/ }
    private void unsetTMFR() { this.flags = flags & (~TMFR);/*unsets the TMFR flag*/ }
    private void unsetFDBU() { this.flags = flags & (~FDBU);/*unsets the FDBU flag*/ }
    private void unsetFDBL() { this.flags = flags & (~FDBL);/*unsets the FDBL flag*/ }

    /**
     * Returns a valid network encoding as a byte array
     *
     * @return networking encoding
     * @throws InvalidPropertiesFormatException if block is not ready to be encoded
     */
    @Override
    byte[] getNetworkEncoding() throws InvalidPropertiesFormatException {
        //todo
        return null;
    }

    /**
     * Checks if a block is completely ready to be encoded (all required fields are set)
     *
     * @return if a block is ready to be encoded
     */
    @Override
    boolean checkBlockFormat() {
        //todo
        //todo:: check readyForTransmission code
        return false;
    }

    /**
     * Sets flags appropriately based on what we know so that checkBlockFormat does not
     * throw an error.  If an unfixable issue is found, raises an error.
     *
     * @throws InvalidAttributesException if block is in an unfixable state
     */
    @Override
    void readyForTransmission(boolean bundleIsAdminRecord, NodeID src) throws InvalidAttributesException {
        if(bundleIsAdminRecord || src.equals(Bundle.NULL_SOURCE)){
            unsetTMFR();
        }
        //todo
        //todo crctype = 0 -> crc = 0

    }
}

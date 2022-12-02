package BPv7.containers;

import javax.naming.directory.InvalidAttributesException;
import java.text.ParseException;
import java.util.InvalidPropertiesFormatException;

/**
 * Represents the first block containing bundle information.
 * @implSpec CBOR array with length:<br>
 *      8: if the bundle is not a fragment and the block has no CRC<br>
 *      9: if the bundle is not a fragment and the block has a CRC<br>
 *      10: if the bundle is a fragment and block has no CRC<br>
 *      11: if the bundle is a fragment and block has a CRC<br>
 * <br>Also note that any received primary block is immutable
 */
public class PrimaryBlock extends Block {
    /**
     * Flag bitmasks
     */
    private final int   FRAG = 0x1,//1 = is a fragment (bundle is fragged)
                        ADMN = 0x2,//1 = admin record
                        NFRG = 0x4,//1 = DO NOT FRAGMENT
                        ACKR = 0x20,//1 = ack requested
                        TIME = 0x40,//1 = status time requested for all status reports
                        //META FLAGS -- request types of status reports
                        RECP = 0x4000,//1 = ACK (basically)
                        FRWD = 0x10000,//1 = send status report when we forward this bundle
                        DLIV = 0x20000,//1 = ACK requested ONLY FROM THE TARGET NODE (status report destination is source)
                        DELT = 0x40000;//1 = send status report if we drop this bundle (like packet drop reports)

    /**
     * BP version (using 7 here)
     */
    protected final int version = 7;//CBOR unsigned
    /**
     * Flags
     *
     * Bits:
     * 0-20 reserved
     * 21-63 unassigned
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
     * Very oversimplified, this is a checksum/hashcode type of thing
     *
     * @implSpec CBOR unsigned int
     * @implNote We didn't do this... But if you feel like
     *   implementing this: "For examples of CRC32C CRCs, see
     *   Appendix A.4 of [RFC7143]"
     */
    protected final int crcType = GLOBAL_CRC_TYPE;
    /**
     * Destination EID (however EIDs are stored in CBOR)
     * Source EID (however EIDs are stored in CBOR)
     *
     * @implSpec CBOR: Check NodeID header
     */
    protected NodeID destNode, srcNode;
    /**
     * Node to send status reports (pertaining to this bundle) to
     *
     * @implSpec CBOR: Check NodeID header
     * @implNote Technically, the specs call for this to only pertain to status
     *   reports about the forwarding and delivery of this bundle (not other
     *   status reports), but we are not utilizing this "feature".
     */
    protected NodeID reportToNode;
    /**
     * Bundle's creation time, uniquely identifies a bundle
     * @implSpec CBOR:: see Timestamp header
     */
    protected Timestamp creationTimestamp;
    /**
     * Time To Live but in ms (delta ms since creation time after which we delete it)
     *
     * @implSpec CBOR unsigned int
     * @implNote a node can override this if it's too long or the node thinks it's part
     *  of a DOS attack. It <b>DOES NOT CHANGE THE FIELD ITSELF</b>, but instead a node
     *  must check if it's gone past the node's BUNDLE_TTL and delete if so. That effectively
     *  acts as the bundle's lifetime while in that node only.
     *  Basically, the node does have to keep a bundle alive for its lifetime if it
     *  doesn't want to (it can drop it sooner).
     */
    protected int lifetime;
    /**
     * Offset from the start of the original bundle the bytes of this bundle's payload
     *  started at
     * @implSpec CBOR unsigned int, present iff FRAG flag is set
     */
    protected int fragmentOffset;
    /**
     * Length of original bundle -- so that if you fragment you know how many frags
     *  to wait for (because length is not recorded in a bundle by default)
     *
     * @implSpec CBOR unsigned int, present iff FRAG flag is set
     */
    protected int ADULen;
    /**
     * CRC value (like a checksum, but it's a hash!)
     *
     * @implSpec CBOR ?? -- depends on CRCType. We use a 2-byte crc:: unsigned 16 bit int<br>
     *  Allowed to not be present if bundle has a BPSEC block.<br>
     *  "The CRC SHALL be computed over the concatenation of all bytes
     *  (including CBOR "break" characters) of the primary block including
     *  the CRC field itself, which, for this purpose, SHALL be temporarily
     *  populated with all bytes set to zero."
     * @implNote <a href="https://crccalc.com/">CRC calculator</a>
     */
    protected short crc;//4 bytes for CRC-16/X-25, which we hardcoded above

    /**
     * Primary constructor with required fields
     * @param destNode destination node EID
     * @param srcNode source node EID
     * @param lifetime delta ms from creation time this bundle can live
     */
    public PrimaryBlock(NodeID destNode, NodeID srcNode, int lifetime) {
        //block number is implicitly 0
        //this.crcType = 1; <- hardcoded
        this.flags = 0;
        this.destNode = destNode;
        this.srcNode = srcNode;
        this.reportToNode = srcNode;
        if(srcNode.equals(Bundle.NULL_SOURCE)){
            //anonymous transmission, RFC specifies the following:
            // NFRG=1, RECP=FWRD=DLIV=DELT=0
            this.setNFRG();
        }
        this.setTimestampToCurr();
        this.lifetime = lifetime;
        this.fragmentOffset = -1;
        this.ADULen = -1;
        this.crc = -1;
    }

    /*getters*/
    public int getVersion() { return version; }
    public long getFlags() { return flags; }
    public int getCrcType() { return crcType; }
    public NodeID getDestNode() { return destNode; }
    public NodeID getSrcNode() { return srcNode; }
    public NodeID getReportToNode() { return reportToNode; }
    public Timestamp getCreationTimestamp() { return creationTimestamp; }
    public int getLifetime() { return lifetime; }
    public int getFragmentOffset() { return fragmentOffset; }
    public int getADULen() { return ADULen; }
    public int getCrc() { return crc; }
    /*flag getters*/
    public bool getFRAG() { return (flags & FRAG) == FRAG;/*gets the FRAG flag*/ }
    public bool getADMN() { return (flags & ADMN) == ADMN;/*gets the ADMN flag*/ }
    public bool getNFRG() { return (flags & NFRG) == NFRG;/*gets the NFRG flag*/ }
    public bool getACKR() { return (flags & ACKR) == ACKR;/*gets the ACKR flag*/ }
    public bool getTIME() { return (flags & TIME) == TIME;/*gets the TIME flag*/ }
    public bool getRECP() { return (flags & RECP) == RECP;/*gets the RECP flag*/ }
    public bool getFRWD() { return (flags & FRWD) == FRWD;/*gets the FRWD flag*/ }
    public bool getDLIV() { return (flags & DLIV) == DLIV;/*gets the DLIV flag*/ }
    public bool getDELT() { return (flags & DELT) == DELT;/*gets the DELT flag*/ }
    
    /*setters*/
    public boolean isAdminRecord(){ return getADMN(); }
    public void setFRAG() { this.flags = flags | FRAG;/*sets the FRAG flag*/ }
    public void setADMN() { this.flags = flags | ADMN;/*sets the ADMN flag*/ }
    public void setNFRG() { this.flags = flags | NFRG;/*sets the NFRG flag*/ }
    public void setACKR() { this.flags = flags | ACKR;/*sets the ACKR flag*/ }
    public void setTIME() { this.flags = flags | TIME;/*sets the TIME flag*/ }
    public void setRECP() { this.flags = flags | RECP;/*sets the RECP flag*/ }
    public void setFRWD() { this.flags = flags | FRWD;/*sets the FRWD flag*/ }
    public void setDLIV() { this.flags = flags | DLIV;/*sets the DLIV flag*/ }
    public void setDELT() { this.flags = flags | DELT;/*sets the DELT flag*/ }
    //unsetters :)
    private void unsetFRAG() { this.flags = flags & (~FRAG);/*unsets the FRAG flag*/ }
    private void unsetADMN() { this.flags = flags & (~ADMN);/*unsets the ADMN flag*/ }
    private void unsetNFRG() { this.flags = flags & (~NFRG);/*unsets the NFRG flag*/ }
    private void unsetACKR() { this.flags = flags & (~ACKR);/*unsets the ACKR flag*/ }
    private void unsetTIME() { this.flags = flags & (~TIME);/*unsets the TIME flag*/ }
    private void unsetRECP() { this.flags = flags & (~RECP);/*unsets the RECP flag*/ }
    private void unsetFRWD() { this.flags = flags & (~FRWD);/*unsets the FRWD flag*/ }
    private void unsetDLIV() { this.flags = flags & (~DLIV);/*unsets the DLIV flag*/ }
    private void unsetDELT() { this.flags = flags & (~DELT);/*unsets the DELT flag*/ }

    //you will know if you need this
    public void setReportToNode(NodeID reportToNode) { this.reportToNode = reportToNode; }
    public void setADULen(int ADULen) { this.ADULen = ADULen; }
    public void setCrc(short crc) { this.crc = crc; }

    /**
     * Set the fragment offset AND sets the FRAG flag
     * @param fragmentOffset Offset from the start of the original bundle the bytes of this bundle's payload
     *  started at
     */
    public void setFragmentOffset(int fragmentOffset) { this.fragmentOffset = fragmentOffset; setFRAG(); }

    //NO!  We are hardcoding this
    private void setCrcType() { }

    //we don't think these are necessary because they are provided in the constructor, if you need them... think carefully
//    public void setDestNode(NodeID destNode) { this.destNode = destNode; }
//    public void setSrcNode(NodeID srcNode) { this.srcNode = srcNode; }
//    public void setLifetime(int lifetime) { this.lifetime = lifetime; }


    /**
     * Sets the creation time to right now
     */
    public void setTimestampToCurr(){ this.creationTimestamp.creationTime = DTNTime.getCurrentDTNTime(); }

    /**
     * Returns a valid network encoding as a byte array
     *
     * @return networking encoding
     * @throws InvalidPropertiesFormatException if block is not ready to be encoded
     */
    @Override
    public byte[] getNetworkEncoding() throws InvalidPropertiesFormatException {
        //todo
        return null;
    }

    /**
     * Decodes the byte array into the implementing object (each class only responsible for its own decoding)
     *
     * @param toDecode network-encoded array to decode
     * @return instance of implementing class with fields populated from toDecode
     * @throws ParseException if invalid input (bad formatting, not enough fields, too many fields, etc)
     */
    @Override
    public NetworkSerializable deserializeNetworkEncoding(byte[] toDecode) throws ParseException {
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
        if(this.isAdminRecord() || this.srcNode.equals(Bundle.NULL_SOURCE)){
            this.reportToNode = Bundle.NULL_SOURCE;
            //anonymous transmission, RFC specifies the following:
            // NFRG=1, RECP=FRWD=DLIV=DELT=0
            unsetRECP(); unsetFRAG(); unsetDLIV(); unsetDELT();
            if(this.srcNode.equals(Bundle.NULL_SOURCE)){//not allowed to fragment if anonymous sender
                setNFRG();
            }
        }
        //todo crctype = 0 -> crc = 0

        //todo
    }
}

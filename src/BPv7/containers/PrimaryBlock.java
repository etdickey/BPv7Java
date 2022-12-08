package BPv7.containers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.naming.directory.InvalidAttributesException;
import java.io.IOException;
import java.text.ParseException;
import java.util.InvalidPropertiesFormatException;
import java.util.logging.Logger;

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
    private static final int
                        FRAG = 0x1,//1 = is a fragment (bundle is fragged)
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
     * @implSpec CBOR unsigned int (32 bit?)
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

    /**
     * Creates a primaryblock based on parameters. Primarily for Jackson JSON serialization
     * @param flags flags
     * @param destNode destination node
     * @param srcNode source node
     * @param reportToNode node to report to if different from source
     * @param creationTimestamp creation timestamp
     * @param lifetime lifetime of bundle
     * @param fragmentOffset fragment offset, if fragment
     * @param ADULen original bundle size, if fragment
     * @param crc CRC (fancy checksum)
     */
    @JsonCreator
    public PrimaryBlock(@JsonProperty("flags") long flags,
                        @JsonProperty("destNode") NodeID destNode,
                        @JsonProperty("srcNode") NodeID srcNode,
                        @JsonProperty("reportToNode") NodeID reportToNode,
                        @JsonProperty("creationTimestamp") Timestamp creationTimestamp,
                        @JsonProperty("lifetime") int lifetime,
                        @JsonProperty("fragmentOffset") int fragmentOffset,
                        @JsonProperty("ADULen") int ADULen,
                        @JsonProperty("crc") short crc) {
        this.flags = flags;
        this.destNode = destNode;
        this.srcNode = srcNode;
        this.reportToNode = reportToNode;
        this.creationTimestamp = creationTimestamp;
        this.lifetime = lifetime;
        this.fragmentOffset = fragmentOffset;
        this.ADULen = ADULen;
        this.crc = crc;
    }

    /*getters*/
    @JsonIgnore
    public int getVersion() { return version; }
    @JsonIgnore
    public int getCrcType() { return crcType; }
    public long getFlags() { return flags; }
    public NodeID getDestNode() { return destNode; }
    public NodeID getSrcNode() { return srcNode; }
    public NodeID getReportToNode() { return reportToNode; }
    public Timestamp getCreationTimestamp() { return creationTimestamp; }
    public int getLifetime() { return lifetime; }
    public int getFragmentOffset() { return fragmentOffset; }
    public int getADULen() { return ADULen; }
    public int getCrc() { return crc; }
    /*flag getters*/
    @JsonIgnore
    public boolean getFRAG() { return (flags & FRAG) == FRAG;/*gets the FRAG flag*/ }
    @JsonIgnore
    public boolean getADMN() { return (flags & ADMN) == ADMN;/*gets the ADMN flag*/ }
    @JsonIgnore
    public boolean getNFRG() { return (flags & NFRG) == NFRG;/*gets the NFRG flag*/ }
    @JsonIgnore
    public boolean getACKR() { return (flags & ACKR) == ACKR;/*gets the ACKR flag*/ }
    @JsonIgnore
    public boolean getTIME() { return (flags & TIME) == TIME;/*gets the TIME flag*/ }
    @JsonIgnore
    public boolean getRECP() { return (flags & RECP) == RECP;/*gets the RECP flag*/ }
    @JsonIgnore
    public boolean getFRWD() { return (flags & FRWD) == FRWD;/*gets the FRWD flag*/ }
    @JsonIgnore
    public boolean getDLIV() { return (flags & DLIV) == DLIV;/*gets the DLIV flag*/ }
    @JsonIgnore
    public boolean getDELT() { return (flags & DELT) == DELT;/*gets the DELT flag*/ }
    
    /*setters*/
    @JsonIgnore
    public boolean isAdminRecord() { return getADMN(); }
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

    /**
     * [use carefully]
     * [do not use for new bundle, use constructor]
     *
     * called to increase the lifetime of the bundle in milliseconds
     * @param lifetime value by which bundle lifetime increased (in milliseconds)
     */
    public void addLifetime(int lifetime) {
        this.lifetime += lifetime;
    }


    /**
     * Sets the creation time to right now
     * todo:: update sequencenum.. static class variable probably would work
     */
    public void setTimestampToCurr(){ this.creationTimestamp = new Timestamp(DTNTime.getCurrentDTNTime(), 0); }

    /**
     * Returns a valid network encoding as a byte array
     *
     * @param logger logger to log things with (containers don't get loggers -- no context)
     * @return networking encoding
     * @throws InvalidPropertiesFormatException if block is not ready to be encoded
     */
    @JsonIgnore
    @Override
    public byte[] getNetworkEncoding(final Logger logger) throws InvalidPropertiesFormatException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(this);//no logging, done only in Bundle
        } catch (JsonProcessingException e) {
            logger.severe("ERROR! Unable to write PrimaryBlock to byte[]: " + e.getMessage());
            throw new InvalidPropertiesFormatException(e.getMessage());
        }
    }

    /**
     * Decodes the byte array into the implementing object (each class only responsible for its own decoding)
     *
     * @param toDecode network-encoded array to decode
     * @param logger logger to log things with (containers don't get loggers -- no context)
     * @return instance of implementing class with fields populated from toDecode
     * @throws ParseException if invalid input (bad formatting, not enough fields, too many fields, etc)
     */
    @JsonIgnore
    @Override
    public NetworkSerializable deserializeNetworkEncoding(byte[] toDecode, final Logger logger) throws ParseException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(toDecode, PrimaryBlock.class);//no logging, done only in Bundle
        } catch (IOException e) {
            logger.severe("ERROR! Unable to read a PrimaryBlock from byte array: " + e.getMessage());
            throw new ParseException(e.getMessage(), -1);
        }
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

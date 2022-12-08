package BPv7.containers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Contains all the information pertinent to a BP bundle
 * @implSpec CBOR:: todo (array?)
 */
public class Bundle implements NetworkSerializable {
    /** ID of the null source, per BP specs */
    public static final NodeID NULL_SOURCE = NodeID.getNullSourceID();
    //todo:: A single transmission request is uniquely identified by {source node ID, bundle creation timestamp}
    //   equals and hashcode

    //todo:: parsing:: rules:
    //  1. An implementation of the Bundle Protocol WILL discard any sequence of bytes that does not conform to the Bundle Protocol specification.
    //  2. must output in this order: [primary, other..., payload, CBOR "break" stop code]

    /** All blocks in this bundle except primary and payload blocks */
    private List<CanonicalBlock> blocks = null;
    /** Primary block (bundle header information) */
    private PrimaryBlock primary = null;
    /** Payload block (actual content being sent) */
    private PayloadBlock payload = null;

    /**
     * Constructor
     */
    public Bundle(PrimaryBlock primary){
        //todo: everything else
        this.primary = primary;
        setTimestampToCurrentTime();
    }

    /**
     * Constructs a Bundle based on parameters (primary for Jackson JSON serializer)
     * @param primary primary block
     * @param payload payload block
     * @param blocks any other blocks
     */
    @JsonCreator
    public Bundle(@JsonProperty("primary") PrimaryBlock primary,
                  @JsonProperty("payload") PayloadBlock payload,
                  @JsonProperty("blocks") List<CanonicalBlock> blocks){
        this.primary = primary;
        this.payload = payload;
        this.blocks = blocks;
    }

    /**
     * ONLY FOR USE WHEN CALLING getNetworkSerialization() !!!!
     * DO NOT USE FOR ANY OTHER PURPOSE!
     */
    public Bundle(){ /* DO NOT USE*/ }

//    /**
//     * Inserts the given block and assigns it an ID
//     * @param b block to insert
//     * @return id of block in bundle
//     * @throws InvalidPropertiesFormatException if block is not valid (after blockid is added)
//     * @throws InvalidParameterException if passed a primary or payload block (use the setters for that)
//     */
//    public int insertBlock(Block b) throws InvalidPropertiesFormatException, InvalidParameterException {
//        //todo (adding ID logic)
//        if(b.getClass() != PrimaryBlock.class && b.getClass() != PayloadBlock.class){
//            //todo:: add id
//        } else {
//            //todo:: ensure you don't already have a primary or payload block,
//            //  otherwise throw InvalidParameterException (must use setters explicitly to be sure you mean it)
//        }
//
//        //todo:: insert (if not payload or primary)
//
//        return -1;
//    }
//
//    /**
//     * Inserts the given admin record and assigns it an ID
//     * @param a admin record to insert
//     * @return id of admin record in bundle
//     * @throws InvalidPropertiesFormatException if block is not valid (after blockid is added)
//     * @throws InvalidParameterException if passed a primary or payload block (use the setters for that)
//     */
//    public int insertAdminRecord(AdminRecord a) throws InvalidPropertiesFormatException, InvalidParameterException {
//        primary.setADMN();
//        //todo parse admin record into a payload (admin record should have a function for that)
//
//        //todo then call insertBlock after we make it into a payload block
//
//        return -1;
//    }

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
            byte[] ret = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(this);
            logger.info("Wrote this bundle as JSON:\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this));
            return ret;
        } catch (JsonProcessingException e) {
            logger.severe("ERROR! Unable to write bundle to byte[]: " + e.getMessage());
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
    @Override
    public Bundle deserializeNetworkEncoding(byte[] toDecode, final Logger logger) throws ParseException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            Bundle ret = mapper.readValue(toDecode, Bundle.class);
            logger.info("Deserialized bundle ID = \"" + ret.getLoggingBundleId() + "\"");
            return ret;
        } catch (IOException e) {
            logger.severe("ERROR! Unable to read a bundle from byte array: " + e.getMessage());
            throw new ParseException(e.getMessage(), -1);
        }
    }



    //getters and setters for explicitly the primary and payload blocks
    public PrimaryBlock getPrimary() { return primary; }
    public final PayloadBlock getPayload() { return payload; }//final because you should use the setter to set a new payload
    /** this must be done in the constructor */
    private  void setPrimary(PrimaryBlock primary) { ; }
    public void setPayload(PayloadBlock payload) { this.payload = payload; }
    @JsonIgnore
    public CanonicalBlock getBlock(int blockID){
        //todo
        return null;
    }

    /**
     * Sets the creation time to right now (called anytime and in the constructor by default)
     */
    public void setTimestampToCurrentTime(){ primary.setTimestampToCurr(); }


    /**
     * Generates a bundle logging id for the given bundle, of the form:
     * [SRC NODE ID]:[CREATION TIMESTAMP in MS]:[SEQ NUMBER]
     * @return the bundle logging ID
     */
    @JsonIgnore
    public String getLoggingBundleId() {
        return "from:" + this.getPrimary().getSrcNode().id() + "::to:" + this.getPrimary().getDestNode().id()
                + "::creationTime:" + this.getPrimary().getCreationTimestamp().creationTime().getTimeInMS()
                + "::seqNum:" + this.getPrimary().getCreationTimestamp().seqNum();
    }
}

package BPv7.containers;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

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
    private List<CanonicalBlock> blocks;
    /** Primary block (bundle header information) */
    private PrimaryBlock primary;
    /** Payload block (actual content being sent) */
    private PayloadBlock payload;

    /**
     * Constructor
     */
    public Bundle(){
        //todo: everything else
        primary = new PrimaryBlock(null, null, -1);
        setTimestampToCurrentTime();
    }




    /**
     * Inserts the given block and assigns it an ID
     * @param b block to insert
     * @return id of block in bundle
     * @throws InvalidPropertiesFormatException if block is not valid (after blockid is added)
     * @throws InvalidParameterException if passed a primary or payload block (use the setters for that)
     */
    public int insertBlock(Block b) throws InvalidPropertiesFormatException, InvalidParameterException {
        //todo (adding ID logic)
        if(b.getClass() != PrimaryBlock.class && b.getClass() != PayloadBlock.class){
            //todo:: add id
        } else {
            //todo:: ensure you don't already have a primary or payload block,
            //  otherwise throw InvalidParameterException (must use setters explicitly to be sure you mean it)
        }

        //todo:: insert (if not payload or primary)

        return -1;
    }

    /**
     * Inserts the given admin record and assigns it an ID
     * @param a admin record to insert
     * @return id of admin record in bundle
     * @throws InvalidPropertiesFormatException if block is not valid (after blockid is added)
     * @throws InvalidParameterException if passed a primary or payload block (use the setters for that)
     */
    public int insertAdminRecord(AdminRecord a) throws InvalidPropertiesFormatException, InvalidParameterException {
        primary.setADMN();
        //todo parse admin record into a payload (admin record should have a function for that)

        //todo then call insertBlock after we make it into a payload block

        return -1;
    }

    /**
     * Returns a valid network encoding as a byte array
     *
     * @return networking encoding
     * @throws InvalidPropertiesFormatException if block is not ready to be encoded
     */
    @Override
    public byte[] getNetworkEncoding() throws InvalidPropertiesFormatException {
        //todo
        return new byte[0];
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





    //getters and setters for explicitly the primary and payload blocks
    public PrimaryBlock getPrimary() { return primary; }
    public final PayloadBlock getPayload() { return payload; }//final because you should use the setter to set a new payload
    public void setPrimary(PrimaryBlock primary) { this.primary = primary; }
    public void setPayload(PayloadBlock payload) { this.payload = payload; }
    public CanonicalBlock getBlock(int blockID){
        //todo
        return null;
    }

    /**
     * Sets the creation time to right now (called anytime and in the constructor by default)
     */
    public void setTimestampToCurrentTime(){ primary.setTimestampToCurr(); }
}

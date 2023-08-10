package BPv7.containers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.ParseException;
import java.util.InvalidPropertiesFormatException;
import java.util.logging.Logger;

/**
 * Represents the PayloadBlock
 */
public class PayloadBlock extends CanonicalBlock {
    /** Block types for this type of block */
    private static final int PAYLOAD_BLOCK_TYPE = 1, PAYLOAD_BLOCK_NUM = 1;
    /** What this block contains */
    protected byte[] payload;

    /** Default constructor */
    @JsonCreator
    public PayloadBlock(@JsonProperty("payload") byte[] payload) {
        super(PAYLOAD_BLOCK_TYPE, PAYLOAD_BLOCK_NUM);
        this.payload = payload;
    }

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
            logger.severe("ERROR! Unable to write PayloadBlock to byte[]: " + e.getMessage());
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
    public NetworkSerializable deserializeNetworkEncoding(byte[] toDecode, final Logger logger) throws ParseException {
        //todo:: figure out which subclass to instantiate and finish decoding with
        //  isn't this done automatically?
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(toDecode, PayloadBlock.class);//no logging, done only in Bundle
        } catch (IOException e) {
            logger.severe("ERROR! Unable to read a PayloadBlock from byte array: " + e.getMessage());
            throw new ParseException(e.getMessage(), -1);
        }
    }

    //getters and setters
    public byte[] getPayload() { return payload; }
    public void setPayload(byte[] payload) { this.payload = payload; }
}

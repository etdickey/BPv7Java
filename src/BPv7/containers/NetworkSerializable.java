package BPv7.containers;

import java.io.Serializable;
import java.text.ParseException;
import java.util.InvalidPropertiesFormatException;

/**
 * Defines a type for being able to serialize things to the network
 * todo:: make every container implement this (not every single one:
 *  block instead of each subclass to force all extending it, etc)
 */
public abstract class NetworkSerializable {
    /**
     * Returns a valid network encoding as a byte array
     *
     * @return networking encoding
     * @throws InvalidPropertiesFormatException if block is not ready to be encoded
     */
    abstract byte[] getNetworkEncoding() throws InvalidPropertiesFormatException;

    /**
     * Decodes the byte array into the implementing object (each class only responsible for its own decoding)
     * @param toDecode network-encoded array to decode
     * @return instance of implementing class with fields populated from toDecode
     * @throws ParseException if invalid input (bad formatting, not enough fields, too many fields, etc)
     */
    abstract NetworkSerializable deserializeNetworkEncoding(byte[] toDecode) throws ParseException;
}

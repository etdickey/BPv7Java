package BPv7.containers;

import java.text.ParseException;
import java.util.InvalidPropertiesFormatException;
import java.util.logging.Logger;

/**
 * Defines a type for being able to serialize things to the network
 * todo:: make every container implement this (not every single one:
 *  block instead of each subclass to force all extending it, etc)
 */
public interface NetworkSerializable {
    /**
     * Returns a valid network encoding as a byte array
     *
     * @param logger logger to log things with (containers don't get loggers -- no context)
     * @return networking encoding
     * @throws InvalidPropertiesFormatException if block is not ready to be encoded
     */
     byte[] getNetworkEncoding(final Logger logger) throws InvalidPropertiesFormatException;

    /**
     * Decodes the byte array into the implementing object (each class only responsible for its own decoding)
     * @param toDecode network-encoded array to decode
     * @param logger logger to log things with (containers don't get loggers -- no context)
     * @return instance of implementing class with fields populated from toDecode
     * @throws ParseException if invalid input (bad formatting, not enough fields, too many fields, etc)
     */
    NetworkSerializable deserializeNetworkEncoding(byte[] toDecode, final Logger logger) throws ParseException;
}

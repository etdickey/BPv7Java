package BPv7.containers;

import java.util.InvalidPropertiesFormatException;

/**
 * Defines a type for being able to serialize things to the network
 */
public abstract class NetworkSerializable {
    /**
     * Returns a valid network encoding as a byte array
     *
     * @return networking encoding
     * @throws InvalidPropertiesFormatException if block is not ready to be encoded
     */
    abstract byte[] getNetworkEncoding() throws InvalidPropertiesFormatException;
}

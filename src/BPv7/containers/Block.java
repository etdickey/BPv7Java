package BPv7.containers;

import javax.naming.directory.InvalidAttributesException;
import java.util.InvalidPropertiesFormatException;

/**
 * Parent class for primary blocks and canonical blocks
 */
public abstract class Block {
    /**
     * Hardset because we don't want to deal with anything else
     */
    protected static final int GLOBAL_CRC_TYPE = 1;

    //other functions
    /**
     * Returns a valid network encoding as a byte array
     * @return networking encoding
     * @throws InvalidPropertiesFormatException if block is not ready to be encoded
     */
    abstract byte[] getNetworkEncoding() throws InvalidPropertiesFormatException;

    /**
     * Checks if a block is completely ready to be encoded (all required fields are set)
     * @return if a block is ready to be encoded
     */
    abstract boolean checkBlockFormat();

    /**
     * Sets flags appropriately based on what we know so that checkBlockFormat does not
     *   throw an error.  If an unfixable issue is found, raises an error.
     * @param bundleIsAdminRecord if bundle is an admin record
     * @param src source node ID
     * @throws InvalidAttributesException if block is in an unfixable state
     */
    abstract void readyForTransmission(boolean bundleIsAdminRecord, NodeID src) throws InvalidAttributesException;
}
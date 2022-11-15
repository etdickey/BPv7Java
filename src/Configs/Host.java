package Configs;

import java.security.InvalidParameterException;

/**
 * Identifies which host we are running.  Relevant mostly to SendStrings and statistics.
 */
public enum Host {
    HOSTA(0, "First host, primary sender and primary driver of statistics"),
    FORWARDINGHOST(1, "Middle host, primary forwarding host and main interest for bottleneck statistics"),
    HOSTB(2, "Second host, primary receiver. Of primary interest for verification of bundle administrative requirements");

    /** Value for Configs.Host */
    private final int val;
    /** Explains the purpose of the host */
    private final String purpose;

    /**
     * Constructs a Configs.Host with a value and associated purpose
     * @param val value for the enum
     * @param purpose purpose of the host
     */
    Host(int val, String purpose){ this.val = val; this.purpose = purpose; }

    /**
     * Get the hostID associated with the given hostID value
     * @param hostID host value
     * @return HostID associated with given value
     * @throws InvalidParameterException if hostID value is out of range
     */
    public static Host getHost(int hostID) throws InvalidParameterException {
        for(Host h : Host.values()){
            if(h.val == hostID){
                return h;//compiler will optimize automatically
            }
        }
        throw new InvalidParameterException("Invalid HostID: " + hostID);
    }

    /** @return value associated with the host */
    public int getVal() { return val; }

    /** @return host's purpose */
    public String getPurpose() { return purpose; }
}

package Configs;

import java.security.InvalidParameterException;

/**
 * Identifies which host we are running.  Relevant mostly to SendStrings and statistics.
 */
public enum Host {
    HOST_A(0, "a", "First host, primary sender and primary driver of statistics"),
    HOST_FORWARDING(1, "forwarding", "Middle host, primary forwarding host and main interest for bottleneck statistics"),
    HOST_B(2, "b", "Second host, primary receiver. Of primary interest for verification of bundle administrative requirements");

    /** Value for Configs.Host */
    private final int val;
    /** Explains the purpose of the host */
    private final String purpose;
    /** Name of host for configuration translation purposes */
    private final String name;

    /**
     * Constructs a Configs.Host with a value and associated purpose
     * @param val value for the enum
     * @param name name of host
     * @param purpose purpose of the host
     */
    Host(int val, String name, String purpose){ this.val = val; this.name = name; this.purpose = purpose; }

    /**
     * Get the Host associated with the given hostID value
     * @param hostID host value
     * @return Host associated with given value
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

    /**
     * Get the Host associated with the given hostname value
     * @param hostname host name
     * @return Host associated with given value
     * @throws InvalidParameterException if hostID value is out of range
     */
    public static Host getHost(String hostname) throws InvalidParameterException {
        for(Host h : Host.values()){
            if(h.name.equalsIgnoreCase(hostname)){
                return h;//compiler will optimize automatically
            }
        }
        throw new InvalidParameterException("Invalid Hostname: " + hostname);

    }

    /** @return value associated with the host */
    public int getVal() { return val; }

    /** @return host's purpose */
    public String getPurpose() { return purpose; }

    /** @return host's name */
    public String getName() { return name; }
}

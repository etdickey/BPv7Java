package BPv7;

import BPv7.interfaces.ApplicationAgentInterface;

import java.util.logging.Logger;

/**
 * Contains all functionality for using BP from the application-level perspective.
 * Also starts up the Administrative Element thread.
 */
public class ApplicationAgent implements ApplicationAgentInterface {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(ApplicationAgent.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static ApplicationAgent instance = null;

    /**
     * Gets an instance of the simulation parameters, also sets up convergence layer parameters
     *
     * @return a reference to the simulation parameters instance
     * @implNote not making this.instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static ApplicationAgent getInstance(){
        if(instance == null){
            synchronized (ApplicationAgent.class){
                if(instance == null){
                    instance = new ApplicationAgent();
                    logger.info("Created ApplicationAgent singleton");
                }
            }
        }
        return instance;
    }

    /**
     * Constructs an ApplicationAgent (singleton will only be called once, so will only spawn one other thread)
     */
    protected ApplicationAgent(){
        //todo:: start new thread with AdminElement (BPA has syntax for threads already if you need a reference)
        //  some other random things that might help:
        //  AdminElement instance = AdminElement.getInstance();
        //  instance.run();
    }

    /**
     * [blocking] Sends a message to the BPA to bundle and send to the end destination
     *
     * @param packetToSend message to send
     * @return packet ID or -1 if packetToSend is Null or BP layer is full
     */
    @Override
    public int send(byte[] packetToSend) {
        //todo:: interface with BPA
        return 0;
    }

    /**
     * Returns the next message from the stream.
     * Calls BPA::getPayload once and saves all the payload in a buffer to return to user at some point
     *
     * @param numToRead number of bytes to read from the stream
     * @return byte[] of size numToRead
     */
    @Override
    public byte[] read(int numToRead) {
        //todo:: interface with BPA
        return new byte[0];
    }

    /**
     * Checks if the packet ID passed in has been sent to the next hop
     * Will trash record of complete transmission after >=1 minute (config file)
     *
     * @param packetID ID of packet to check
     * @return true if the “packet” has reached the next hop ONLY (else false)
     */
    @Override
    public boolean checkSent(int packetID) {
        //todo:: interface with BPA
        return false;
    }
}

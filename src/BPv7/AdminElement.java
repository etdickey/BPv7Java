package BPv7;

import BPv7.interfaces.AdminElementInterface;

import java.util.logging.Logger;

/**
 * Manages BPA administrative operations.  Spawned by ApplicationAgent.
 */
public class AdminElement implements AdminElementInterface {
    /** Logger for this class. Prepends all logs from this class with the class name */
    private static final Logger logger = Logger.getLogger(AdminElement.class.getName());

    /**
     * The only instance of this class allowed in the entire program
     * @implNote not making this volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     *  (caching ok because all variables are final)
     */
    private static AdminElement instance = null;

    /**
     * Gets the singleton instance of the AdminElement driver
     *
     * @return a reference to the administrative element instance
     * @implNote not making this.instance volatile because its value only changes once
     *  (null -> instance), thus only one set of double-checked locking is needed
     */
    public static AdminElementInterface getInstance(){
        if(instance == null){
            synchronized (AdminElement.class){
                if(instance == null){
                    //todo:: anything special you need to set up at the beginning
                    instance = new AdminElement();
                    logger.info("Created AdminElement singleton");
                }
            }
        }
        return instance;
    }

    /**
     * Default constructor, hiding it from anyone else
     */
    protected AdminElement(){}

    /**
     * Spins up a thread that manages BPA administrative operations
     */
    @Override
    public void run() {
        //todo

    }
}

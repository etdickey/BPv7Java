package BPv7.interfaces;

/**
 * Interface for the class that manages administrative activities of the BPA
 * Must be runnable so that we can spin it up in a separate thread
 */
public interface AdminElementInterface extends Runnable {
    /**
     * todo: maintain a Map<nodeID, Boolean> timeReqested; -- if a node we have communicated with has EVER set their
     *     time flag
     * @see BPv7.containers.PrimaryBlock.TIME
     */

    //todo:: rest of stuff an implementing admin class should have publicly accessible
}

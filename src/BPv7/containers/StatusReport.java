package BPv7.containers;
import java.time.*;
public class StatusReport extends AdminRecord{
    /*Getters and setters for status report info*/
    boolean statusIndicator;
    int reasonCode;
    NodeID sourceBundleNodeID;
    LocalTime creationTimestamp; /* These are the four required without fragmentation. There are two more with. */
}

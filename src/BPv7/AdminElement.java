package BPv7;

import BPv7.containers.BundleStatusItem;
import BPv7.containers.NodeID;
import BPv7.containers.StatusReport;
import BPv7.containers.Timestamp;
import BPv7.interfaces.AdminElementInterface;
import BPv7.utils.BundleStatusReport;
import BPv7.utils.StatusReportUtilObject;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
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
        new Thread(() -> {
            try {
                byte[] payload = BPA.readStatusReportBuffer.take();
                StatusReportUtilObject statusReportRevd = (StatusReportUtilObject)bytesToObject(payload);
    
                // O - REC; 1 - FORW; 2 - DELI; 3 - DEL
                BundleStatusReport status = statusReportRevd.getBundleStatusReportEnum();
                BundleStatusItem received = new BundleStatusItem(true);
                BundleStatusItem forwarded = new BundleStatusItem((status == 
                                                 BundleStatusReport.FORWARDED) ? true : false);
                BundleStatusItem delivered = new BundleStatusItem((status == 
                                                 BundleStatusReport.DELIVERED) ? true : false);
                BundleStatusItem deleted = new BundleStatusItem((status == 
                                               BundleStatusReport.DELETED) ? true : false);
                
                // TODO: implement reasonCode
                StatusReport statusReport = new StatusReport(received, forwarded, delivered, deleted,
                            0, statusReportRevd.getSourceNodeID(), statusReportRevd.getBundleTimestamp());
    
                if (status == BundleStatusReport.DELETED) {
                    // package's status is DELETE, resend
                    // TODO: send resend flag to BPA
                }
                else {
                    
                }
    
    
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Convert a byte stream to an object
     */
    public Object bytesToObject(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        Object obj = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);
            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}

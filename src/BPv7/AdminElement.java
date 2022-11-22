package BPv7;

import BPv7.containers.BundleStatusItem;
import BPv7.containers.NodeID;
import BPv7.containers.StatusReport;
import BPv7.containers.Timestamp;
import BPv7.interfaces.AdminElementInterface;
import BPv7.utils.BundleStatusReport;
import BPv7.utils.StatusReportUtilObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
                            this.reasonCode(), statusReportRevd.getSourceNodeID(), statusReportRevd.getBundleTimestamp());
    
                if (status == BundleStatusReport.DELETED) {
                    // package's status is DELETE, resend with current time-stemp
                    // TODO: send resend flag to BPA (Verify)
                    BPA.getInstance().resendBundle(statusReportRevd.getBundleTimestamp());
                }
                else {
                    // TODO: Verify it
                    // TODO: What to reture for nodeID. Previous Node's ID or Src's NodeID, or current Node's NodeID.
                    // TODO: Ask if getSourceNodeID() returns the actual src's NodeID.
                    byte[] b_statusReport = this.objectToByteArray(statusReport);
                    // BPA.getInstance().sendWithAdminFlag(b_statusReport, statusReportRevd.getSourceNodeID()); 
                    BPA.getInstance().sendWithACK(b_statusReport, statusReportRevd.getSourceNodeID());
                }
    
    
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block for b_statusReport
                // If the byte array cannot be generated from the statusReport object, run this
                e.printStackTrace();
            }
        }).start();
    }

    /*
     *     0 -> No additional information.
     *     1 -> Lifetime expired.
     *     2 -> Forwarded over unidirectional link.
     *     3 -> Transmission canceled.
     *     4 -> Depleted storage.
     *     5 -> Destination endpoint ID unavailable.
     *     6 -> No known route to destination from here.
     *     7 -> No timely contact with next node on route.
     *     8 -> Block unintelligible.
     *     9 -> Hop limit exceeded.
     *     10 -> Traffic pared (e.g., status reports).
     *     11 -> Block unsupported.
     *     17-254 -> Unassigned.
     *     255 -> Reserved.
     */

    private int reasonCode() {
        // TODO: what should I do in this function given no clue or evidence?
        return 0;
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

    /**
     * Convert an object to a bytes array
     * @throws IOException
     */
    public byte[] objectToByteArray(StatusReport obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);   
            out.writeObject(obj);
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            return yourBytes;
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }
}

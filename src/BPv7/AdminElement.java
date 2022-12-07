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
     * Basically the 'form filler' for BPA. Builds StatusReport based on the
     * information BPA sent over (StatusReportUtilObject) and sends it back to BPA.
     */
    private final Thread adminElementStatusReportBuilder;

    /**
     * Recommends action based on the reason code that BPA sent over.
     */
    private final Thread adminElementStatusReportRcode;

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
    protected AdminElement() {
        this.adminElementStatusReportBuilder = new Thread(AdminElementStatusReportBuilder.getInstance());
        this.adminElementStatusReportBuilder.start();
//        this.adminElementStatusReportBuilder = AdminElementStatusReportBuilder.getInstance();
//        this.adminElementStatusReportBuilder.getInstance();

        this.adminElementStatusReportRcode = new Thread(AdminElementStatusReportRcode.getInstance());
        this.adminElementStatusReportRcode.start();
    }

    /**
     * Spins up a thread that manages BPA administrative operations
     */
    @Override
    public void run() {}

    /**
     * Convert a byte stream to an object
     */
    public static Object bytesToObject(byte[] bytes) {
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
    public static byte[] objectToByteArray(StatusReport obj) throws IOException {
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

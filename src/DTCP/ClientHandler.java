package DTCP;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import BPv7.containers.Bundle;
import Configs.ConvergenceLayerParams;

class ClientHandler implements Runnable{

    /**
     * The instance of the ConvergenceLayerParams Class
     */
    private final ConvergenceLayerParams config = ConvergenceLayerParams.getInstance();

    /** Logger for this class. Prepends all logs from this class with the class name */
    private final Logger logger = Logger.getLogger(DTCPServer.class.getName());

    /** The socket for this client connection */
    private final Socket client;

    /**
     * The queue for bundles received to offer to the BPA layer.
     * This is package private for ClientHandler.
     */
    private final BlockingQueue<Bundle> outQueue;

    /**
     * The ID of the received bundle (or equivalent, might change later)
     */
    private String bundleID = null;

    /**
     * Constructor for creating a Client Handler
     * @param client the client connection Socket
     * @param outQueue the queue for sending Bundles to the BPA layer
     */
    public ClientHandler(Socket client, BlockingQueue<Bundle> outQueue) {
        this.client = client;
        this.outQueue = outQueue;
    }

    /**
     * The thread method for handling for receiving a bundle
     */
    @Override
    public void run() {
        try {
            byte[] result = client.getInputStream().readAllBytes();

            //May need to change this later
            Bundle bundle = (Bundle) (new Bundle()).deserializeNetworkEncoding(result);
            bundleID = bundle.getPrimary().getSrcNode().id()
                        + ':' + bundle.getPrimary().getCreationTimestamp().getCreationTime().getTimeInMS()
                        + ':' + bundle.getPrimary().getCreationTimestamp().getSeqNum();
            //noinspection ConstantConditions
            if (bundleID != null) { //Should always be the case, but might as well be safe
                logger.log(Level.INFO, "Bundle Received: " + bundleID);
                String srcAddress = ((InetSocketAddress) client.getRemoteSocketAddress()).getAddress().getHostAddress();
                if (DTCPUtils.isConnectionDownUnexpected(srcAddress))
                    logger.log(Level.INFO, "Dropping bundle due to unexpected down: " + bundleID);
                else if (!outQueue.offer(bundle, config.queueTimeoutInMillis, TimeUnit.MILLISECONDS)) {
                    logger.log(Level.INFO, "Queue is full, dropping bundle:" + bundleID);
                }
                else {
                    logger.log(Level.INFO, "Added bundle to queue:" + bundleID);
                }
            }
            else {
                logger.log(Level.WARNING, "Received Malformed Bundle, dropping");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Client IOException, Bundle Dropped");
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Queue Interrupt Expection, Bundle Dropped");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unspecified Failure To Read Bundle and add to Queue");
        }
    }
}

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

/**
 * This class is to handle each client connections coming into the DTCP server, with a new one created for each
 * connection.
 * @implSpec This will drop bundle if: an unexpected down, input queue is full for entire queue timeout, or exceptions
 */
class ClientHandler implements Runnable{

    /**
     * The instance of the ConvergenceLayerParams Class
     */
    private final ConvergenceLayerParams config = ConvergenceLayerParams.getInstance();

    /** Logger for this class. Prepends all logs from this class with the class name */
    private final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    /** The socket for this client connection */
    private final Socket client;

    /**
     * The queue for bundles received to offer to the BPA layer.
     * This is passed into the Client Handler on creation.
     * @implNote all Blocking Queue implementations are guaranteed to be thread safe
     */
    private final BlockingQueue<Bundle> outQueue;

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
            // This blocks until the whole bundle is received. There is a timeout on the socket if it takes too long.
            byte[] result = client.getInputStream().readAllBytes();

            //May need to change this later
            Bundle bundle = (new Bundle()).deserializeNetworkEncoding(result, logger);
            String bundleID = bundle.getLoggingBundleId();
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
        } catch (IOException e) {
            logger.log(Level.WARNING, "Client IOException, Bundle Dropped: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Queue Interrupt Expection, Bundle Dropped: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unspecified Failure To Read Bundle and add to Queue: " + e.getMessage());
        }
    }
}

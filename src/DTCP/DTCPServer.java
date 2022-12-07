package DTCP;

import BPv7.containers.Bundle;
import Configs.ConvergenceLayerParams;
import Configs.SimulationParams;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The server (thread) for receiving and spinning up Client Handlers to handle incoming connections/bundles
 */
class DTCPServer implements Runnable {

    /**
     * The instance of the ConvergenceLayerParams Class
     */
    private final ConvergenceLayerParams convParams = ConvergenceLayerParams.getInstance();

    /**
     * The instance of the SimulationParams Class
     */
    private static final SimulationParams simParams = SimulationParams.getInstance();

    /** Logger for this class. Prepends all logs from this class with the class name */
    private final Logger logger = Logger.getLogger(DTCPServer.class.getName());

    /**
     * The queue for bundles received to offer to the BPA layer.
     */
    private final BlockingQueue<Bundle> outQueue;

    /**
     * Constuctor for the DTCP Server
     * @param outQueue the queue for sending Bundles to the BPA layer
     */
    public DTCPServer(BlockingQueue<Bundle> outQueue) {
        this.outQueue = outQueue;
        //Add Anything Needed Later
    }

    /**
     * The thread method for actually running the server
     */
    public void run() {
        try (ExecutorService threadPool = Executors.newFixedThreadPool(convParams.nThreads)) {
            try (ServerSocket serverSocket = new ServerSocket(simParams.scenario.dtcpPort(), convParams.maxConnections)) {
                logger.log(Level.INFO, "DTCP Server Started");

                //noinspection InfiniteLoopStatement
                while (true) {
                    try (Socket client = serverSocket.accept()) {
                        client.setSoTimeout(convParams.connectionTimeout);
                        threadPool.execute(new ClientHandler(client, outQueue));
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Failed to accept client in DTCP: " + e.getMessage());
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "CRITICAL INTERNAL ERROR:" + e.getMessage());
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "DTCP Server Failed: " + e.getMessage());
                throw new RuntimeException("DTCP Server Failed", e);
                // Not sure how people want me to exit or handle this other than just not accept anything else
            }
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Thread Pool Count Invalid: " + e.getMessage());
        }
    }
}

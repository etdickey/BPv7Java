package BPv7.interfaces;

public interface ApplicationAgent {
    int send(byte[] packetToSend);
    /*::[blocking]
    * Return:packet ID or -1 if packetToSend is Null or BP layer is full
    * */
    byte[] read(int numToRead);
    /*Returns byte[] of size numToRead
    * Calls BPA::getPayload once and saves all the payload in a buffer to return to user at some point
    * */
    boolean checkSent(int packetID);
    /*Return true if the “packet” has reached the next hop ONLY (else false)
    * Will trash record of complete transmission after >=1 minute (config file)
     * */
}

package BPv7.interfaces;

public interface ApplicationAgent {
    int send(byte[] packetToSend);
    /*::[blocking]
    * Return:packet ID or -1 if packetToSend is Null or BP layer is full
    * */
    byte[] read(int numToRead);
    /*Returns byte[] of size numToRead*/
    boolean checkSent(int packetID);
    /*Return true if the “packet” has reached the next hop ONLY (else false)
    * Will trash record of complete transmission after >=1 minute (config file)
     * */
}

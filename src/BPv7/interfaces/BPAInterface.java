package BPv7.interfaces;

import BPv7.containers.AdminRecord;
import BPv7.containers.Bundle;

//todo:: ethan javadoc + move implementation details
public interface BPAInterface {//package-private (not private/public)
    //listener thread
    //   note: discard any sequence of bytes that does not conform to BP

    //todo:: bundle parse

    //todo: defragment function, gets all the fragment bundles and gets their
    // payload and combines it into a single byte stream and feeds it back
    // into the bundle parser

    AdminRecord getAdminRecord();
//    {
//    /*Returns the payload of the next admin bundle (which is just an admin record)*/
//        StatusReport placeholder = new StatusReport();
//        return placeholder;
//    } /*::[blocking]*/

    byte[] getPayload() throws InterruptedException;
//    {
//        /*Returns the next bundle’s entire payload*/
//        byte[] abc = java.util.HexFormat.of().parseHex("e04fd020ea3a6910a2d808002b30309d");
//        return abc; /*variable and return placeholder for required return*/
//    }
    int send(Bundle bundle);

    /*Notes on BPA:
Maintains two buffers, one of admin bundles and one of normal bundles
Needs asynchronous function that waits for new bundles
Not declaring buffers as java seems to need buffer type and I'm unsure if its ByteBuffer type
*/

}

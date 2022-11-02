package BPv7.interfaces;

import BPv7.containers.StatusReport;

public class BPA {

    protected StatusReport getAdminRecord() {
    /*Returns the payload of the next admin bundle (which is just an admin record)*/
        StatusReport placeholder = new StatusReport();
        return placeholder;
    } /*::[blocking]*/

    protected byte[] getPayload() {
        /*Returns the next bundle’s entire payload*/
        byte[] abc = java.util.HexFormat.of().parseHex("e04fd020ea3a6910a2d808002b30309d");
        return abc; /*variable and return placeholder for required return*/
    }

    /*Notes on BPA:
Maintains two buffers, one of admin bundles and one of normal bundles
Needs asynchronous function that waits for new bundles
Not declaring buffers as java seems to need buffer type and I'm unsure if its ByteBuffer type
*/

}

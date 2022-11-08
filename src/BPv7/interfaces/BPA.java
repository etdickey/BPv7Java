package BPv7.interfaces;

import BPv7.containers.Bundle;
import BPv7.containers.StatusReport;

public interface BPA {

    public StatusReport getAdminRecord();

    public byte[] getPayload() throws InterruptedException;

    public int send(Bundle bundle);

}

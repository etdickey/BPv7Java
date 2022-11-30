package BPv7.utils;

import BPv7.containers.Bundle;

public class BundleDispatchStatusMap {
    Bundle bundle;
    DispatchStatus status;

    public BundleDispatchStatusMap(Bundle bundle, DispatchStatus status) {
        this.bundle = bundle;
        this.status = status;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public DispatchStatus getStatus() {
        return status;
    }

    public void setStatus(DispatchStatus status) {
        this.status = status;
    }
}

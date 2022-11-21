package BPv7.utils;

import BPv7.containers.NodeID;
import BPv7.containers.Timestamp;

public class StatusReportUtilObject {

    NodeID sourceNodeID;
    Timestamp bundleTimestamp;
    BundleStatusReport bundleStatusReportEnum;

    public StatusReportUtilObject(NodeID sourceNodeID, Timestamp bundleTimestamp, BundleStatusReport bundleStatusReportEnum) {
        this.sourceNodeID = sourceNodeID;
        this.bundleTimestamp = bundleTimestamp;
        this.bundleStatusReportEnum = bundleStatusReportEnum;
    }

    public NodeID getSourceNodeID() {
        return sourceNodeID;
    }

    public void setSourceNodeID(NodeID sourceNodeID) {
        this.sourceNodeID = sourceNodeID;
    }

    public Timestamp getBundleTimestamp() {
        return bundleTimestamp;
    }

    public void setBundleTimestamp(Timestamp bundleTimestamp) {
        this.bundleTimestamp = bundleTimestamp;
    }

    public BundleStatusReport getBundleStatusReportEnum() {
        return bundleStatusReportEnum;
    }

    public void setBundleStatusReportEnum(BundleStatusReport bundleStatusReportEnum) {
        this.bundleStatusReportEnum = bundleStatusReportEnum;
    }
}

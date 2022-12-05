package BPv7.utils;

import BPv7.containers.NodeID;
import BPv7.containers.Timestamp;

import java.util.Objects;

/**
 * Util record to pass bundle details for status report to admin element
 * @param sourceNodeID source node ID of the bundle
 * @param bundleTimestamp bundle timestamp
 * @param bundleStatusReportEnum bundle status from BPA
 */
public record StatusReportUtilObject (NodeID sourceNodeID, Timestamp bundleTimestamp, BundleStatusReport bundleStatusReportEnum) {
    public StatusReportUtilObject {
        Objects.requireNonNull(sourceNodeID);
        Objects.requireNonNull(bundleTimestamp);
        Objects.requireNonNull(bundleStatusReportEnum);
    }
}

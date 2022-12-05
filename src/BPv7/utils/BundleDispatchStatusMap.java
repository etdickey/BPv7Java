package BPv7.utils;

import BPv7.containers.Bundle;

import java.util.Objects;

/**
 * Util class for keep map of bundle, and it's status
 * @param bundle bundle of BPA
 * @param status status of bundle
 */
public record BundleDispatchStatusMap(Bundle bundle, DispatchStatus status)  {
    public BundleDispatchStatusMap {
        Objects.requireNonNull(bundle);
        Objects.requireNonNull(status);
    }
}

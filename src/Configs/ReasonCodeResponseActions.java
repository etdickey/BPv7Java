package Configs;

/**
 * set of actions to take by Admin Element when responding to bundle loss Status Report
 * True = resend (possibly with modifications), false = drop
 * @param lifetimeExpiredAction           Action when informed of bundle loss in network for ReasonCode = lifetimeExpired
 * @param overUnidirectionalAction        Action when informed of bundle loss in network for ReasonCode = overUnidirectional
 * @param transmissionCancelledAction     Action when informed of bundle loss in network for ReasonCode = transmissionCancelled
 * @param depletedStorageAction           Action when informed of bundle loss in network for ReasonCode = depletedStorage
 * @param destinationUnavailableAction    Action when informed of bundle loss in network for ReasonCode = destinationUnavailable
 * @param noKnownRouteToDestinationAction Action when informed of bundle loss in network for ReasonCode = noKnownRouteToDestination
 * @param noTimelyContactAction           Action when informed of bundle loss in network for ReasonCode = noTimelyContact
 * @param blockUnintelligibleAction       Action when informed of bundle loss in network for ReasonCode = blockUnintelligible
 * @param hopLimitExceededAction          Action when informed of bundle loss in network for ReasonCode = hopLimitExceeded
 * @param trafficParedAction              Action when informed of bundle loss in network for ReasonCode = trafficPared
 * @param blockUnsupportedAction          Action when informed of bundle loss in network for ReasonCode = blockUnsupported
 */
public record ReasonCodeResponseActions(boolean lifetimeExpiredAction, boolean overUnidirectionalAction,
                                        boolean transmissionCancelledAction, boolean depletedStorageAction,
                                        boolean destinationUnavailableAction, boolean noKnownRouteToDestinationAction,
                                        boolean noTimelyContactAction, boolean blockUnintelligibleAction,
                                        boolean hopLimitExceededAction, boolean trafficParedAction,
                                        boolean blockUnsupportedAction) {
    //Admin Element action preferences -- per host
}

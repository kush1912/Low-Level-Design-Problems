package lld.DesignProblems.rideSharingService.publishers;

import lld.DesignProblems.rideSharingService.models.Event;
import lld.DesignProblems.rideSharingService.observers.EventObserver;

public interface EventPublisher {

    void subscribe(String rideId, EventObserver observer);

    void unsubscribe(String rideId, String observerId);

    void unsubscribeAll(String rideId);

    void publish(Event event);
}

package lld.DesignProblems.rideSharingService.services;

import lld.DesignProblems.rideSharingService.models.Event;
import lld.DesignProblems.rideSharingService.models.Ride;
import lld.DesignProblems.rideSharingService.publishers.EventPublisher;

public class EventHandler {
    private final EventPublisher eventPublisher;

    public EventHandler(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void handle(Ride ride, Event event) {
        if (!ride.getRideId().equals(event.getRideId())) {
            throw new IllegalArgumentException(
                    "Event does not belong to the supplied ride"
            );
        }

        ride.addEvent(event);
        eventPublisher.publish(event);
    }

    public void finishRide(Ride ride) {
        ride.finish();
        eventPublisher.unsubscribeAll(ride.getRideId());
    }
}

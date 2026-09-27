package lld.DesignProblems.rideSharingService.publishers;

import lld.DesignProblems.rideSharingService.models.Event;
import lld.DesignProblems.rideSharingService.observers.EventObserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RideEventPublisher implements EventPublisher {
    /*
     * Outer key: ride ID.
     * Outer value: all observers subscribed to that ride.
     * Inner key: observer ID, used to prevent duplicate subscriptions.
     * Inner value: observer that receives the ride's events.
     *
     * ConcurrentHashMap allows location publishing, subscription, and
     * unsubscription to happen safely from different request threads.
     */
    private final Map<String, Map<String, EventObserver>> observersByRide =
            new ConcurrentHashMap<>();

    @Override
    public void subscribe(String rideId, EventObserver observer) {
        observersByRide.computeIfAbsent(rideId,
                        ignored -> new ConcurrentHashMap<>())
                .put(observer.getObserverId(), observer);
    }

    @Override
    public void unsubscribe(String rideId, String observerId) {
        Map<String, EventObserver> observers = observersByRide.get(rideId);
        if (observers == null) {
            return;
        }
        observers.remove(observerId);
        if (observers.isEmpty()) {
            observersByRide.remove(rideId, observers);
        }
    }

    @Override
    public void unsubscribeAll(String rideId) {
        observersByRide.remove(rideId);
    }

    @Override
    public void publish(Event event) {
        Map<String, EventObserver> observers = observersByRide.get(event.getRideId());

        if (observers == null) {
            return;
        }
        observers.values().forEach(
                observer -> observer.onEvent(event)
        );
    }
}

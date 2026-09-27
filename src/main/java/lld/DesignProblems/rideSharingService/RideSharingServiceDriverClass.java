package lld.DesignProblems.rideSharingService;

import lld.DesignProblems.rideSharingService.models.Coordinate;
import lld.DesignProblems.rideSharingService.models.Driver;
import lld.DesignProblems.rideSharingService.models.Event;
import lld.DesignProblems.rideSharingService.models.Ride;
import lld.DesignProblems.rideSharingService.models.Rider;
import lld.DesignProblems.rideSharingService.observers.EventObserver;
import lld.DesignProblems.rideSharingService.observers.SharedRideObserver;
import lld.DesignProblems.rideSharingService.publishers.EventPublisher;
import lld.DesignProblems.rideSharingService.publishers.RideEventPublisher;
import lld.DesignProblems.rideSharingService.services.EventHandler;

public class RideSharingServiceDriverClass {

    public static void main(String[] args) {
        Rider rider = new Rider("RIDER-1", "Ajay");
        Driver driver = new Driver(
                "DRIVER-1",
                "Ravi",
                "MH-01-AB-1234"
        );

        Ride ride = new Ride(
                "RIDE-1",
                "Kotak Office",
                new Coordinate(19.1176, 72.8631),
                "Mumbai Airport",
                new Coordinate(19.0896, 72.8656),
                rider,
                driver
        );

        EventPublisher publisher = new RideEventPublisher();
        EventHandler eventHandler = new EventHandler(publisher);

        publisher.subscribe(ride.getRideId(), rider);

        EventObserver sharedViewer = new SharedRideObserver(
                "VIEWER-1",
                "Priya"
        );
        publisher.subscribe(ride.getRideId(), sharedViewer);

        Event event = new Event(ride.getRideId(), 19.1100, 72.8640);

        eventHandler.handle(ride, event);
        eventHandler.finishRide(ride);

        System.out.printf(
                "Ride %s finished. All observers unsubscribed.%n",
                ride.getRideId()
        );
    }
}

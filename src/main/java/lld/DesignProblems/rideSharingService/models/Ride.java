package lld.DesignProblems.rideSharingService.models;

import lld.DesignProblems.rideSharingService.enums.RideStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Ride {
    private final String rideId;
    private final String sourceName;
    private final Coordinate source;
    private final String destinationName;
    private final Coordinate destination;
    private final Rider rider;
    private final Driver driver;
    private final List<Event> events;

    private RideStatus status;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    public Ride(
            String rideId,
            String sourceName,
            Coordinate source,
            String destinationName,
            Coordinate destination,
            Rider rider,
            Driver driver
    ) {
        this.rideId = rideId;
        this.sourceName = sourceName;
        this.source = source;
        this.destinationName = destinationName;
        this.destination = destination;
        this.rider = rider;
        this.driver = driver;
        this.events = new ArrayList<>();
        this.status = RideStatus.STARTED;
        this.startTime = LocalDateTime.now();
    }

    public void addEvent(Event event) {
        if (status != RideStatus.STARTED) {
            throw new IllegalStateException(
                    "Events can only be added to a started ride"
            );
        }

        events.add(event);
    }

    public void finish() {
        if (status != RideStatus.STARTED) {
            throw new IllegalStateException(
                    "Only a started ride can be finished"
            );
        }

        status = RideStatus.FINISHED;
        finishTime = LocalDateTime.now();
    }
}

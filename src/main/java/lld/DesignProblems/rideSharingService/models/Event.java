package lld.DesignProblems.rideSharingService.models;

import lombok.Getter;

@Getter
public class Event {
    private final String rideId;
    private final double latitude;
    private final double longitude;

    public Event(
            String rideId,
            double latitude,
            double longitude
    ) {
        this.rideId = rideId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

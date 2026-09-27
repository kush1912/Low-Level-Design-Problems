package lld.DesignProblems.rideSharingService.models;

import lld.DesignProblems.rideSharingService.observers.EventObserver;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Rider implements EventObserver {
    private final String userId;
    private final String name;
    private final List<String> rideIds;

    public Rider(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.rideIds = new ArrayList<>();
    }

    @Override
    public String getObserverId() {
        return userId;
    }

    @Override
    public void onEvent(Event event) {
        System.out.printf(
                "%s received location for ride %s: (%s, %s)%n",
                name,
                event.getRideId(),
                event.getLatitude(),
                event.getLongitude()
        );
    }
}

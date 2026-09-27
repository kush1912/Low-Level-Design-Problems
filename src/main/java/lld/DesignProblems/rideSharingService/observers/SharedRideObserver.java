package lld.DesignProblems.rideSharingService.observers;

import lld.DesignProblems.rideSharingService.models.Event;

public class SharedRideObserver implements EventObserver {
    private final String observerId;
    private final String name;

    public SharedRideObserver(String observerId, String name) {
        this.observerId = observerId;
        this.name = name;
    }

    @Override
    public String getObserverId() {
        return observerId;
    }

    @Override
    public void onEvent(Event event) {
        System.out.printf(
                "%s received shared ride %s location: (%s, %s)%n",
                name,
                event.getRideId(),
                event.getLatitude(),
                event.getLongitude()
        );
    }
}

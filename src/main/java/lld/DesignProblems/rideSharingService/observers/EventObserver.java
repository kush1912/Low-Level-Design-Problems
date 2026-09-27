package lld.DesignProblems.rideSharingService.observers;

import lld.DesignProblems.rideSharingService.models.Event;

public interface EventObserver {

    String getObserverId();

    void onEvent(Event event);
}

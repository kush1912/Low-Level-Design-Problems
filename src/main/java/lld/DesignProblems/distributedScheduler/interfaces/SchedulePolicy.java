package lld.DesignProblems.distributedScheduler.interfaces;

import java.time.Instant;
import java.util.Optional;

public interface SchedulePolicy {
    Instant firstExecutionAt();

    Optional<Instant> nextExecutionAfter(Instant scheduledAt, int currentOccurrence);
}

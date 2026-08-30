package lld.DesignProblems.distributedScheduler.schedules;

import lld.DesignProblems.distributedScheduler.interfaces.SchedulePolicy;

import java.time.Instant;
import java.util.Optional;

public record OneTimeSchedulePolicy(Instant executeAt) implements SchedulePolicy {

    @Override
    public Instant firstExecutionAt() {
        return executeAt;
    }

    // A one-time schedule has no next occurrence; empty avoids using null for this valid outcome.
    @Override
    public Optional<Instant> nextExecutionAfter(Instant scheduledAt, int currentOccurrence) {
        return Optional.empty();
    }
}

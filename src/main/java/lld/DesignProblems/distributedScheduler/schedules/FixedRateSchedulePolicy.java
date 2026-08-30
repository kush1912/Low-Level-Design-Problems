package lld.DesignProblems.distributedScheduler.schedules;

import lld.DesignProblems.distributedScheduler.interfaces.SchedulePolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public record FixedRateSchedulePolicy(
        Instant firstExecutionAt,
        Duration interval,
        int maxExecutions
) implements SchedulePolicy {

    public FixedRateSchedulePolicy {
        if (interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("Interval must be positive");
        }
        if (maxExecutions < 1) {
            throw new IllegalArgumentException("Maximum executions must be at least 1");
        }
    }

    @Override
    public Instant firstExecutionAt() {
        return firstExecutionAt;
    }

    @Override
    public Optional<Instant> nextExecutionAfter(Instant scheduledAt, int currentOccurrence) {
        if (currentOccurrence >= maxExecutions) {
            return Optional.empty();
        }
        return Optional.of(scheduledAt.plus(interval));
    }
}

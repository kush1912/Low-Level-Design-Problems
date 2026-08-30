package lld.DesignProblems.distributedScheduler.models;

import lld.DesignProblems.distributedScheduler.enums.TriggerStatus;
import lld.DesignProblems.distributedScheduler.enums.TriggerType;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class JobExecutionTrigger implements Delayed {
    private final String id;
    private final String jobId;
    private final Optional<String> scheduleId;
    private final TriggerType type;
    private final Instant triggerAt;
    private final int occurrence;
    private final long sequence;
    private final AtomicReference<TriggerStatus> status;

    private JobExecutionTrigger(
            String id,
            String jobId,
            Optional<String> scheduleId,
            TriggerType type,
            Instant triggerAt,
            int occurrence,
            long sequence) {
        this.id = id;
        this.jobId = jobId;
        this.scheduleId = scheduleId;
        this.type = type;
        this.triggerAt = triggerAt;
        this.occurrence = occurrence;
        this.sequence = sequence;
        this.status = new AtomicReference<>(TriggerStatus.PENDING);
    }

    // Scheduled Trigger
    public static JobExecutionTrigger scheduled(String id, String jobId, String scheduleId, Instant triggerAt, int occurrence, long sequence) {
        return new JobExecutionTrigger(id, jobId, Optional.of(scheduleId), TriggerType.SCHEDULED, triggerAt, occurrence, sequence);
    }

    // Manual Trigger
    public static JobExecutionTrigger manual(String id, String jobId, Instant triggerAt, long sequence) {
        return new JobExecutionTrigger(id, jobId, Optional.empty(), TriggerType.MANUAL, triggerAt, 0, sequence);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long remainingMillis = triggerAt.toEpochMilli() - System.currentTimeMillis();
        return unit.convert(remainingMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        JobExecutionTrigger trigger = (JobExecutionTrigger) other;

        int timeComparison = triggerAt.compareTo(trigger.triggerAt);
        if (timeComparison != 0) {
            return timeComparison;
        }

        return Long.compare(sequence, trigger.sequence);
    }

    public String id() {
        return id;
    }

    public String jobId() {
        return jobId;
    }

    public Optional<String> scheduleId() {
        return scheduleId;
    }

    public TriggerType type() {
        return type;
    }

    public Instant triggerAt() {
        return triggerAt;
    }

    public int occurrence() {
        return occurrence;
    }

    public TriggerStatus status() {
        return status.get();
    }

    public boolean claim() {
        return status.compareAndSet(TriggerStatus.PENDING, TriggerStatus.CLAIMED);
    }

    public boolean cancel() {
        return status.compareAndSet(TriggerStatus.PENDING, TriggerStatus.CANCELLED);
    }


}

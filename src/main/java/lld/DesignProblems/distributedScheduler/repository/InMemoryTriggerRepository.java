package lld.DesignProblems.distributedScheduler.repository;

import lld.DesignProblems.distributedScheduler.models.JobExecutionTrigger;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTriggerRepository {
    private final ConcurrentHashMap<String, JobExecutionTrigger> pendingTriggers =
            new ConcurrentHashMap<>();

    public void save(String scheduleId, JobExecutionTrigger trigger) {
        pendingTriggers.put(scheduleId, trigger);
    }

    public Optional<JobExecutionTrigger> findByScheduleId(String scheduleId) {
        return Optional.ofNullable(pendingTriggers.get(scheduleId));
    }

    public Optional<JobExecutionTrigger> remove(String scheduleId) {
        return Optional.ofNullable(pendingTriggers.remove(scheduleId));
    }

    public boolean remove(String scheduleId, JobExecutionTrigger trigger) {
        return pendingTriggers.remove(scheduleId, trigger);
    }
}

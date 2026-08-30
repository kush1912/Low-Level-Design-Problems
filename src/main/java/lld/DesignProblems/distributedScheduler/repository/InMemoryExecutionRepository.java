package lld.DesignProblems.distributedScheduler.repository;

import lld.DesignProblems.distributedScheduler.models.JobExecution;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryExecutionRepository {
    private final ConcurrentHashMap<String, JobExecution> executions =
            new ConcurrentHashMap<>();

    public void save(JobExecution execution) {
        executions.put(execution.id(), execution);
    }

    public Optional<JobExecution> findById(String executionId) {
        return Optional.ofNullable(executions.get(executionId));
    }
}

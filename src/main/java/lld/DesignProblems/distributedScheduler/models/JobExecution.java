package lld.DesignProblems.distributedScheduler.models;

import lld.DesignProblems.distributedScheduler.enums.ExecutionStatus;

import java.util.concurrent.atomic.AtomicReference;

public final class JobExecution {
    private final String id;
    private final String jobId;
    private final AtomicReference<ExecutionStatus> status;

    public JobExecution(String id, String jobId) {
        this.id = id;
        this.jobId = jobId;
        this.status = new AtomicReference<>(ExecutionStatus.QUEUED);
    }

    public String id() {
        return id;
    }

    public String jobId() {
        return jobId;
    }

    public ExecutionStatus status() {
        return status.get();
    }

    public boolean markRunning() {
        return status.compareAndSet(ExecutionStatus.QUEUED, ExecutionStatus.RUNNING);
    }

    public boolean markCompleted() {
        return status.compareAndSet(ExecutionStatus.RUNNING, ExecutionStatus.COMPLETED);
    }

    public boolean markFailed() {
        return status.compareAndSet(ExecutionStatus.RUNNING, ExecutionStatus.FAILED);
    }

    public boolean cancel() {
        return status.compareAndSet(ExecutionStatus.QUEUED, ExecutionStatus.CANCELLED);
    }
}

package lld.DesignProblems.distributedScheduler.models;

import lld.DesignProblems.distributedScheduler.interfaces.SchedulePolicy;

public final class Schedule {
    private final String id;
    private final String jobId;
    private volatile SchedulePolicy policy;

    public Schedule(String id, String jobId, SchedulePolicy policy) {
        this.id = id;
        this.jobId = jobId;
        this.policy = policy;
    }

    public String id() {
        return id;
    }

    public String jobId() {
        return jobId;
    }

    public SchedulePolicy policy() {
        return policy;
    }

    public void updatePolicy(SchedulePolicy newPolicy) {
        policy = newPolicy;
    }
}

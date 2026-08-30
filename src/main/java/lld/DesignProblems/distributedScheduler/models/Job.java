package lld.DesignProblems.distributedScheduler.models;

import lld.DesignProblems.distributedScheduler.interfaces.JobTask;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class Job {
    private final String id;
    private final JobTask task;

    // Index schedules by ID for average O(1) lookup, update, and removal instead of an O(n) list scan.
    private final ConcurrentHashMap<String, Schedule> schedules;

    public Job(String id, JobTask task) {
        this.id = id;
        this.task = task;
        this.schedules = new ConcurrentHashMap<>();
    }

    public String id() {
        return id;
    }

    public JobTask task() {
        return task;
    }

    public void addSchedule(Schedule schedule) {
        schedules.put(schedule.id(), schedule);
    }

    public Optional<Schedule> findSchedule(String scheduleId) {
        return Optional.ofNullable(schedules.get(scheduleId));
    }

    public boolean removeSchedule(String scheduleId) {
        return schedules.remove(scheduleId) != null;
    }

    public List<Schedule> schedules() {
        return List.copyOf(schedules.values());
    }
}

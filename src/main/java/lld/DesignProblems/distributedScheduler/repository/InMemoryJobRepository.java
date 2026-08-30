package lld.DesignProblems.distributedScheduler.repository;

import lld.DesignProblems.distributedScheduler.models.Job;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryJobRepository {
    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    public void save(Job job) {
        jobs.put(job.id(), job);
    }

    public Optional<Job> findById(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public boolean remove(String jobId) {
        return jobs.remove(jobId) != null;
    }
}

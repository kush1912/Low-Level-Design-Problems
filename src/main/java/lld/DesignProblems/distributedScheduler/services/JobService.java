package lld.DesignProblems.distributedScheduler.services;

import lld.DesignProblems.distributedScheduler.helper.IdGenerator;
import lld.DesignProblems.distributedScheduler.interfaces.JobTask;
import lld.DesignProblems.distributedScheduler.models.Job;
import lld.DesignProblems.distributedScheduler.repository.InMemoryJobRepository;

import java.time.Duration;
import java.time.Instant;

public final class JobService {
    private final InMemoryJobRepository jobRepository;

    public JobService(InMemoryJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public String createJob(JobTask task) {
        Job job = new Job(IdGenerator.generate("J"), task);
        jobRepository.save(job);
        return job.id();
    }

    public String createTimedJob(String name, Duration duration) {
        //  code inside the lambda does not run while calling createTimedJob().
        //  It runs only later when JobWorker calls JobTask.execute()
        return createJob(() -> {
            System.out.println(name + " started at " + Instant.now()
                    + " on " + Thread.currentThread().getName());
            Thread.sleep(duration);
            System.out.println(name + " finished at " + Instant.now());
        });
    }
}

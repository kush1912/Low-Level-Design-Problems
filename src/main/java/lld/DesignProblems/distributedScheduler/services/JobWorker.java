package lld.DesignProblems.distributedScheduler.services;

import lld.DesignProblems.distributedScheduler.models.Job;
import lld.DesignProblems.distributedScheduler.models.JobExecution;
import lld.DesignProblems.distributedScheduler.repository.InMemoryJobRepository;

public final class JobWorker {
    private final InMemoryJobRepository jobRepository;

    public JobWorker(InMemoryJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public void execute(JobExecution execution) {
        Job job = jobRepository.findById(execution.jobId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid job ID: " + execution.jobId()));

        if (!execution.markRunning()) {
            return;
        }

        try {
            job.task().execute();
            execution.markCompleted();
            System.out.println("Job " + job.id() + " completed");
        } catch (InterruptedException exception) {
            execution.markFailed();
            Thread.currentThread().interrupt();
            System.err.println("Job " + job.id() + " was interrupted");
        } catch (Exception exception) {
            execution.markFailed();
            System.err.println("Job " + job.id() + " failed: " + exception.getMessage());
        }
    }
}

package lld.DesignProblems.distributedScheduler.services;

import lld.DesignProblems.distributedScheduler.helper.IdGenerator;
import lld.DesignProblems.distributedScheduler.enums.TriggerStatus;
import lld.DesignProblems.distributedScheduler.models.Job;
import lld.DesignProblems.distributedScheduler.models.JobExecutionTrigger;
import lld.DesignProblems.distributedScheduler.queue.JobExecutionTriggerQueue;
import lld.DesignProblems.distributedScheduler.repository.InMemoryExecutionRepository;
import lld.DesignProblems.distributedScheduler.repository.InMemoryJobRepository;
import lld.DesignProblems.distributedScheduler.repository.InMemoryTriggerRepository;
import lld.DesignProblems.distributedScheduler.schedules.FixedRateSchedulePolicy;
import lld.DesignProblems.distributedScheduler.schedules.OneTimeSchedulePolicy;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerServiceTest {

    @Test
    void executesJobWhenScheduledTriggerBecomesDue() throws InterruptedException {
        InMemoryJobRepository jobRepository = new InMemoryJobRepository();
        InMemoryTriggerRepository triggerRepository = new InMemoryTriggerRepository();
        InMemoryExecutionRepository executionRepository = new InMemoryExecutionRepository();
        JobExecutionTriggerQueue triggerQueue = new JobExecutionTriggerQueue();
        ExecutorService workerPool = Executors.newFixedThreadPool(2);
        JobWorker jobWorker = new JobWorker(jobRepository);
        SchedulerService schedulerService = new SchedulerService(
                jobRepository,
                triggerRepository,
                executionRepository,
                triggerQueue,
                jobWorker,
                workerPool
        );
        CountDownLatch executed = new CountDownLatch(1);
        Job job = new Job(IdGenerator.generate("J"), executed::countDown);
        jobRepository.save(job);

        try {
            schedulerService.start();
            schedulerService.createSchedule(
                    job.id(),
                    new OneTimeSchedulePolicy(Instant.now().plusMillis(100))
            );

            assertTrue(executed.await(2, TimeUnit.SECONDS));
        } finally {
            schedulerService.shutdown();
        }
    }

    @Test
    void stopsFixedRateScheduleAfterMaximumExecutions() throws InterruptedException {
        InMemoryJobRepository jobRepository = new InMemoryJobRepository();
        InMemoryTriggerRepository triggerRepository = new InMemoryTriggerRepository();
        InMemoryExecutionRepository executionRepository = new InMemoryExecutionRepository();
        JobExecutionTriggerQueue triggerQueue = new JobExecutionTriggerQueue();
        ExecutorService workerPool = Executors.newFixedThreadPool(2);
        SchedulerService schedulerService = new SchedulerService(
                jobRepository,
                triggerRepository,
                executionRepository,
                triggerQueue,
                new JobWorker(jobRepository),
                workerPool
        );
        CountDownLatch executions = new CountDownLatch(3);
        String jobId = new JobService(jobRepository).createJob(executions::countDown);

        try {
            schedulerService.start();
            schedulerService.createSchedule(
                    jobId,
                    new FixedRateSchedulePolicy(
                            Instant.now().plusMillis(50),
                            java.time.Duration.ofMillis(50),
                            3
                    )
            );

            assertTrue(executions.await(2, TimeUnit.SECONDS));
        } finally {
            schedulerService.shutdown();
        }
    }

    @Test
    void runsJobImmediatelyWithoutChangingItsSchedule() throws InterruptedException {
        InMemoryJobRepository jobRepository = new InMemoryJobRepository();
        InMemoryTriggerRepository triggerRepository = new InMemoryTriggerRepository();
        InMemoryExecutionRepository executionRepository = new InMemoryExecutionRepository();
        JobExecutionTriggerQueue triggerQueue = new JobExecutionTriggerQueue();
        ExecutorService workerPool = Executors.newFixedThreadPool(2);
        SchedulerService schedulerService = new SchedulerService(
                jobRepository,
                triggerRepository,
                executionRepository,
                triggerQueue,
                new JobWorker(jobRepository),
                workerPool
        );
        CountDownLatch execution = new CountDownLatch(1);
        String jobId = new JobService(jobRepository).createJob(execution::countDown);
        String scheduleId = schedulerService.createSchedule(
                jobId,
                new OneTimeSchedulePolicy(Instant.now().plusSeconds(10))
        );

        try {
            schedulerService.start();
            schedulerService.runNow(jobId);

            assertTrue(execution.await(2, TimeUnit.SECONDS));
            assertTrue(triggerRepository.findByScheduleId(scheduleId).isPresent());
        } finally {
            schedulerService.shutdown();
        }
    }

    @Test
    void updateScheduleCancelsPendingTriggerAndUsesNewPolicy() throws InterruptedException {
        InMemoryJobRepository jobRepository = new InMemoryJobRepository();
        InMemoryTriggerRepository triggerRepository = new InMemoryTriggerRepository();
        InMemoryExecutionRepository executionRepository = new InMemoryExecutionRepository();
        JobExecutionTriggerQueue triggerQueue = new JobExecutionTriggerQueue();
        ExecutorService workerPool = Executors.newFixedThreadPool(2);
        SchedulerService schedulerService = new SchedulerService(
                jobRepository,
                triggerRepository,
                executionRepository,
                triggerQueue,
                new JobWorker(jobRepository),
                workerPool
        );
        CountDownLatch execution = new CountDownLatch(1);
        String jobId = new JobService(jobRepository).createJob(execution::countDown);
        String scheduleId = schedulerService.createSchedule(
                jobId,
                new OneTimeSchedulePolicy(Instant.now().plusSeconds(10))
        );
        JobExecutionTrigger originalTrigger =
                triggerRepository.findByScheduleId(scheduleId).orElseThrow();

        try {
            schedulerService.start();
            schedulerService.updateSchedule(
                    jobId,
                    scheduleId,
                    new OneTimeSchedulePolicy(Instant.now().plusMillis(50))
            );

            assertEquals(TriggerStatus.CANCELLED, originalTrigger.status());
            assertTrue(execution.await(2, TimeUnit.SECONDS));
        } finally {
            schedulerService.shutdown();
        }
    }
}

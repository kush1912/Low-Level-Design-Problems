package lld.DesignProblems.distributedScheduler;

import lld.DesignProblems.distributedScheduler.queue.JobExecutionTriggerQueue;
import lld.DesignProblems.distributedScheduler.repository.InMemoryExecutionRepository;
import lld.DesignProblems.distributedScheduler.repository.InMemoryJobRepository;
import lld.DesignProblems.distributedScheduler.repository.InMemoryTriggerRepository;
import lld.DesignProblems.distributedScheduler.schedules.FixedRateSchedulePolicy;
import lld.DesignProblems.distributedScheduler.schedules.OneTimeSchedulePolicy;
import lld.DesignProblems.distributedScheduler.services.JobService;
import lld.DesignProblems.distributedScheduler.services.JobWorker;
import lld.DesignProblems.distributedScheduler.services.SchedulerService;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobSchedulerDriverClass {

    public static void main(String[] args) throws InterruptedException {

        // Initialize the repositories, Queue and Services
        InMemoryJobRepository jobRepository = new InMemoryJobRepository();
        InMemoryTriggerRepository triggerRepository = new InMemoryTriggerRepository();
        InMemoryExecutionRepository executionRepository = new InMemoryExecutionRepository();

        JobExecutionTriggerQueue triggerQueue = new JobExecutionTriggerQueue();

        // It can execute 3 workers concurrently - actual execution of the task!!
        ExecutorService workerPool = Executors.newFixedThreadPool(3);

        //Scheduler Service
        SchedulerService schedulerService = new SchedulerService(jobRepository, triggerRepository, executionRepository, triggerQueue, new JobWorker(jobRepository), workerPool);
        JobService jobService = new JobService(jobRepository);

        //Job 1
        String twentySecondJob = jobService.createTimedJob("20-second job", Duration.ofSeconds(20));

        //Job2
        String thirtySecondJob = jobService.createTimedJob("30-second job", Duration.ofSeconds(30));

        //Job3
        String fortySecondJob = jobService.createTimedJob("40-second job", Duration.ofSeconds(40));


        Instant commonStartTime = Instant.now().plusSeconds(2);

        //Scheduler 1
        schedulerService.createSchedule(twentySecondJob, new FixedRateSchedulePolicy(commonStartTime, Duration.ofSeconds(60), 5));

        //Scheduler 2
        schedulerService.createSchedule(thirtySecondJob, new FixedRateSchedulePolicy(commonStartTime, Duration.ofSeconds(60), 5)
        );

        //Scheduler 3
        schedulerService.createSchedule(fortySecondJob, new OneTimeSchedulePolicy(commonStartTime));

        //Start the Service
        schedulerService.start();
        System.out.println("Scheduler started. All three jobs first run at " + commonStartTime);

        try {
            // This sleep only keeps the demo process alive while scheduled jobs execute.
            Thread.sleep(Duration.ofMinutes(6));
        } finally {
            schedulerService.shutdown();
        }
        System.out.println("Scheduler demo finished");
    }
}

package lld.DesignProblems.distributedScheduler.services;

import lld.DesignProblems.distributedScheduler.enums.TriggerType;
import lld.DesignProblems.distributedScheduler.helper.IdGenerator;
import lld.DesignProblems.distributedScheduler.interfaces.SchedulePolicy;
import lld.DesignProblems.distributedScheduler.models.Job;
import lld.DesignProblems.distributedScheduler.models.JobExecution;
import lld.DesignProblems.distributedScheduler.models.JobExecutionTrigger;
import lld.DesignProblems.distributedScheduler.models.Schedule;
import lld.DesignProblems.distributedScheduler.queue.JobExecutionTriggerQueue;
import lld.DesignProblems.distributedScheduler.repository.InMemoryExecutionRepository;
import lld.DesignProblems.distributedScheduler.repository.InMemoryJobRepository;
import lld.DesignProblems.distributedScheduler.repository.InMemoryTriggerRepository;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class SchedulerService {
    private final InMemoryJobRepository jobRepository;
    private final InMemoryTriggerRepository triggerRepository;
    private final InMemoryExecutionRepository executionRepository;
    private final JobExecutionTriggerQueue triggerQueue;
    private final JobWorker jobWorker;

    // Thread pool for executing actual Jobs
    private final ExecutorService workerPool;
    private final AtomicLong sequence = new AtomicLong();

    //Should the scheduler consumer continue running?
    private final AtomicBoolean running = new AtomicBoolean(); // Initialised to false

    //This stores a reference to the actual thread running consumeTriggers()
    // is volatile to guarantee that its latest reference is visible to every thread
    private volatile Thread schedulerThread;

    public SchedulerService(
            InMemoryJobRepository jobRepository,
            InMemoryTriggerRepository triggerRepository,
            InMemoryExecutionRepository executionRepository,
            JobExecutionTriggerQueue triggerQueue,
            JobWorker jobWorker,
            ExecutorService workerPool) {
        this.jobRepository = jobRepository;
        this.triggerRepository = triggerRepository;
        this.executionRepository = executionRepository;
        this.triggerQueue = triggerQueue;
        this.jobWorker = jobWorker;
        this.workerPool = workerPool;
    }

    public String createSchedule(String jobId, SchedulePolicy policy) {

        //get Job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown job: " + jobId));

        // Create Schedule
        Schedule schedule = new Schedule(IdGenerator.generate("S"), jobId, policy);

        // Creating a trigger for that Schedule
        JobExecutionTrigger trigger = JobExecutionTrigger
                .scheduled(IdGenerator.generate("T"), jobId, schedule.id(), policy.firstExecutionAt(), 1, sequence.incrementAndGet());

        synchronized (job) {
            job.addSchedule(schedule);
            triggerRepository.save(schedule.id(), trigger);

            // Producer Code adding it to Queue
            triggerQueue.add(trigger);
        }

        return schedule.id();
    }

    public String runNow(String jobId) {
        jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown job: " + jobId));

        JobExecutionTrigger trigger = JobExecutionTrigger.manual(
                IdGenerator.generate("T"),
                jobId,
                Instant.now(),
                sequence.incrementAndGet()
        );
        triggerQueue.add(trigger);
        return trigger.id();
    }

    public void updateSchedule(String jobId, String scheduleId, SchedulePolicy newPolicy) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown job: " + jobId));

        synchronized (job) {
            Schedule schedule = job.findSchedule(scheduleId)
                    .orElseThrow(() -> new NoSuchElementException(
                            "Cannot update missing schedule: " + scheduleId
                    ));

            schedule.updatePolicy(newPolicy);
            triggerRepository.remove(scheduleId)
                    .ifPresent(JobExecutionTrigger::cancel);

            JobExecutionTrigger replacementTrigger = JobExecutionTrigger.scheduled(
                    IdGenerator.generate("T"),
                    jobId,
                    scheduleId,
                    newPolicy.firstExecutionAt(),
                    1,
                    sequence.incrementAndGet()
            );
            triggerRepository.save(scheduleId, replacementTrigger);
            triggerQueue.add(replacementTrigger);
        }
    }


    // Start the Scheduler
    public void start() {
        // Starting the Consumer by setting it to true only if it was false
        // Threadsafe so started by 1 thread and closed by 1 thread
        if (!running.compareAndSet(false, true)) {
            return;
        }
        schedulerThread = new Thread(this::consumeTriggers, "job-scheduler");
        schedulerThread.start();
    }

    // Shutdown the Scheduler
    public void shutdown() {
        // Shutting down the consumer
        if (!running.compareAndSet(true, false)) {
            return;
        }

        //Without synchronization or volatile, Java does not guarantee that the shutdown caller immediately sees the reference assigned by the start caller.
        // It could theoretically observe the old value:
        Thread thread = schedulerThread;
        if (thread != null) {
            thread.interrupt();
        }
        workerPool.shutdown();
    }

    // Consumer Code
    private void consumeTriggers() {

        // Infinite loop based on running variable to run the consumer
        while (running.get()) {
            try {
                JobExecutionTrigger trigger = triggerQueue.take();
                processTrigger(trigger);
            } catch (NoSuchElementException exception) {
                System.err.println(exception.getMessage());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // handles the trigger that is due right now
    private void processTrigger(JobExecutionTrigger trigger) {
        if (!trigger.claim()) {
            return;
        }

        enqueueNextTrigger(trigger);

        JobExecution execution = new JobExecution(IdGenerator.generate("E"), trigger.jobId());
        executionRepository.save(execution);

        try {
            // Actually execute the work
            workerPool.submit(() -> jobWorker.execute(execution));
        } catch (RejectedExecutionException exception) {
            execution.cancel();
            System.err.println("Worker pool rejected execution " + execution.id());
        }
    }


    // creates the next occurrence for a recurring schedule
    private void enqueueNextTrigger(JobExecutionTrigger currentTrigger) {
        if (currentTrigger.type() != TriggerType.SCHEDULED) {
            return;
        }

        Job job = jobRepository.findById(currentTrigger.jobId())
                .orElseThrow(() -> new NoSuchElementException("Cannot reschedule trigger because job does not exist: " + currentTrigger.jobId()));


        String scheduleId = currentTrigger.scheduleId()
                .orElseThrow(() -> new NoSuchElementException("Scheduled trigger has no schedule ID: " + currentTrigger.id()));

        synchronized (job) {
            // A schedule update replaces the pending trigger. In that case, this claimed
            // trigger may execute, but it must not create another recurring trigger.
            if (!triggerRepository.remove(scheduleId, currentTrigger)) {
                return;
            }

            Schedule schedule = job.findSchedule(scheduleId)
                    .orElseThrow(() -> new NoSuchElementException("Cannot reschedule missing schedule: " + scheduleId));

            //Ask the policy whether another occurrence exists; if it does, create and enqueue its trigger. Otherwise, the schedule is complete.
            schedule.policy()
                    .nextExecutionAfter(currentTrigger.triggerAt(), currentTrigger.occurrence())
                    .ifPresent(nextExecutionAt -> {
                        JobExecutionTrigger nextTrigger = JobExecutionTrigger.
                                scheduled(IdGenerator.generate("T"), currentTrigger.jobId(), scheduleId,
                                        nextExecutionAt, currentTrigger.occurrence() + 1, sequence.incrementAndGet());

                        triggerRepository.save(scheduleId, nextTrigger);
                        triggerQueue.add(nextTrigger);
                    });
        }
    }
}

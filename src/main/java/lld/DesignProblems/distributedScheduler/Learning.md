# Distributed Scheduler Learning Notes

## 1. What each concrete class represents

**`JobTask`** defines the actual work through its `execute()` method.

**`Job`** answers **what should run**. It is a reusable definition, analogous to a program, so it does not have execution status.

**`Schedule`** answers **when and how often a job should run**. It owns a timing policy, not runtime execution status.

**`OneTimeSchedulePolicy`** provides one execution time and no next occurrence.

**`FixedRateSchedulePolicy`** provides a first time, fixed interval, and maximum execution count.

**`JobExecutionTrigger`** represents one occurrence waiting to become due:

```text
Run this job at this time.
```

It has `PENDING`, `CLAIMED`, and `CANCELLED` states because claiming and cancellation can race.

**`JobExecution`** is the actual runtime attempt, analogous to a process. It has:

```text
QUEUED -> RUNNING -> COMPLETED
                  -> FAILED
QUEUED -> CANCELLED
```

**`JobService`** creates jobs. **`SchedulerService`** manages schedules and triggers. **`JobWorker`** performs the actual execution.

**Repositories** store jobs, pending triggers, and executions. **`JobExecutionTriggerQueue`** wraps the `DelayQueue`.

Remember:

```text
Job       = reusable work definition
Schedule  = when and how often
Trigger   = one occurrence waiting for its time
Execution = actual runtime attempt
```

## 2. Core idea

An in-memory scheduler is a time-aware producer-consumer system:

```text
createSchedule()/runNow()
        |
        v
JobExecutionTrigger
        |
        v
DelayQueue
        |
        v
scheduler consumer thread
        |
        v
JobExecution
        |
        v
ExecutorService worker pool
        |
        v
JobTask.execute()
```

- Schedule APIs produce triggers.
- `DelayQueue.take()` returns a trigger only when it is due.
- The scheduler claims the trigger and creates an execution.
- A worker thread runs the actual business task.
- The scheduler thread never executes long-running business logic.

## 3. Current domain model

```text
Job
 |- String id
 |- JobTask task
 `- Map<String, Schedule>

Schedule
 |- String id
 |- String jobId
 `- volatile SchedulePolicy

JobExecutionTrigger
 |- String id
 |- String jobId
 |- Optional<String> scheduleId
 |- TriggerType
 |- Instant triggerAt
 |- int occurrence
 |- long sequence
 `- AtomicReference<TriggerStatus>

JobExecution
 |- String id
 |- String jobId
 `- AtomicReference<ExecutionStatus>
```

Relationships:

```text
One Job -> many Schedules
One Schedule -> one pending trigger
Schedule or manual request -> Trigger
Claimed Trigger -> JobExecution
JobExecution -> executes JobTask once
```

## 4. JobTask and JobService

`JobTask` is a functional interface:

```java
@FunctionalInterface
public interface JobTask {
    void execute() throws Exception;
}
```

A lambda can define the work:

```java
String jobId = jobService.createJob(
        () -> emailService.sendEmail()
);
```

`createJob()` only stores the task:

```java
public String createJob(JobTask task) {
    Job job = new Job(IdGenerator.generate("J"), task);
    jobRepository.save(job);
    return job.id();
}
```

The task runs later when `JobWorker` calls:

```java
job.task().execute();
```

`createTimedJob()` is only a driver convenience for creating tasks that sleep for a configured duration.

## 5. Schedule and SchedulePolicy

`Schedule` connects a job to a timing policy:

```java
public interface SchedulePolicy {
    Instant firstExecutionAt();

    Optional<Instant> nextExecutionAfter(
            Instant scheduledAt,
            int currentOccurrence
    );
}
```

Current policies:

```java
new OneTimeSchedulePolicy(executeAt);

new FixedRateSchedulePolicy(
        firstExecutionAt,
        Duration.ofSeconds(60),
        5
);
```

One-time policy:

```text
firstExecutionAt()      -> configured time
nextExecutionAfter()    -> Optional.empty()
```

Fixed-rate policy:

```text
next time = current scheduled time + interval
```

It is based on scheduled time rather than completion time. The policy returns `Optional.empty()` after `maxExecutions` is reached.

Policies are immutable records and do not need status. `Schedule.policy` is `volatile` because an update replaces the policy reference.

## 6. Recurring execution

Every scheduled trigger contains an occurrence number:

```text
Occurrence 1 -> enqueue occurrence 2
Occurrence 2 -> enqueue occurrence 3
Occurrence 3 -> enqueue occurrence 4
Occurrence 4 -> enqueue occurrence 5
Occurrence 5 -> stop
```

The policy determines whether another occurrence exists:

```java
schedule.policy()
        .nextExecutionAfter(
                currentTrigger.triggerAt(),
                currentTrigger.occurrence()
        )
        .ifPresent(nextExecutionAt -> {
            // Create and enqueue the next trigger.
        });
```

An empty result is valid for one-time schedules and completed recurring schedules.

## 7. Manual execution

`runNow(jobId)`:

1. Verifies that the job exists.
2. Creates a manual trigger due at `Instant.now()`.
3. Adds it directly to the trigger queue.

A manual trigger:

- Has no schedule ID.
- Uses occurrence zero.
- Is not stored as a pending scheduled trigger.
- Does not change existing schedules.
- Does not produce another occurrence.

A manual and scheduled execution of the same `JobTask` may run concurrently, so the task must be thread-safe unless overlap prevention is added.

## 8. Trigger and DelayQueue

`JobExecutionTrigger` implements `Delayed`:

```java
@Override
public long getDelay(TimeUnit unit) {
    long remainingMillis =
            triggerAt.toEpochMilli() - System.currentTimeMillis();
    return unit.convert(remainingMillis, TimeUnit.MILLISECONDS);
}
```

`DelayQueue` is:

- Thread-safe.
- Ordered by trigger time.
- Blocking when empty.
- Blocking when the earliest trigger is not due.

`sequence` breaks ties when triggers have the same execution time.

```text
PriorityQueue         -> ordered, not thread-safe, no time waiting
PriorityBlockingQueue -> ordered, thread-safe, no time waiting
DelayQueue            -> ordered, thread-safe, waits until due
```

## 9. Creating a schedule: producer flow

`createSchedule()`:

1. Loads the job.
2. Creates a `Schedule`.
3. Creates occurrence one as a trigger.
4. Stores the schedule in the job.
5. Stores the trigger as the pending trigger.
6. Adds the trigger to the `DelayQueue`.

```text
createSchedule()
    -> create Schedule
    -> create first Trigger
    -> triggerRepository.save()
    -> triggerQueue.add()
```

This is the producer side of the first queue.

## 10. Starting and stopping the consumer

```java
private final AtomicBoolean running = new AtomicBoolean();
private volatile Thread schedulerThread;
```

`running.compareAndSet(false, true)` prevents multiple scheduler consumers from being started.

```java
private void consumeTriggers() {
    while (running.get()) {
        JobExecutionTrigger trigger = triggerQueue.take();
        processTrigger(trigger);
    }
}
```

`schedulerThread` is `volatile` so the shutdown caller sees the latest thread reference.

Shutdown performs:

```text
running = false              -> asks the loop to stop
schedulerThread.interrupt()  -> wakes it from DelayQueue.take()
```

The driver decides when to start and stop. `SchedulerService` owns how its internal thread is managed.

## 11. Processing a due trigger

`processTrigger()`:

```text
claim trigger
    -> enqueue next occurrence if required
    -> create JobExecution
    -> save execution
    -> submit to worker pool
```

Claiming is atomic:

```text
PENDING -> CLAIMED
PENDING -> CANCELLED
```

Only one transition can win. Cancelled or previously claimed triggers are ignored.

The execution is submitted without blocking the scheduler:

```java
workerPool.submit(() -> jobWorker.execute(execution));
```

The scheduler immediately returns to waiting for the next trigger.

## 12. Updating a schedule

`updateSchedule(jobId, scheduleId, newPolicy)`:

1. Loads the job and schedule.
2. Synchronizes on the job.
3. Replaces the schedule policy.
4. Removes and cancels the pending trigger.
5. Creates occurrence one from the new policy.
6. Stores and enqueues the replacement trigger.

A running execution is not interrupted because it was already created from a claimed trigger.

Conditional removal prevents duplicate recurrence:

```java
if (!triggerRepository.remove(scheduleId, currentTrigger)) {
    return;
}
```

If an update has already replaced the pending trigger, the old claimed trigger may execute but cannot create another next occurrence.

## 13. Optional usage

Use `Optional` based on whether absence is valid:

- Required value: `orElseThrow()`.
- Valid absence: `ifPresent()`, `map()`, or `Optional.empty()`.
- Avoid `orElse(null)`.

Required job:

```java
Job job = jobRepository.findById(jobId)
        .orElseThrow(() ->
                new IllegalArgumentException("Unknown job: " + jobId));
```

Valid optional schedule ID:

```java
trigger.scheduleId().ifPresent(scheduleId ->
        triggerRepository.remove(scheduleId, trigger)
);
```

A manual trigger legitimately has no schedule ID.

## 14. Current driver scenario

The driver creates:

```text
20-second job -> every 60 seconds
30-second job -> every 60 seconds
40-second job -> one time
```

All initially have the same trigger time and execute through a three-thread worker pool.

During execution:

1. The driver waits 25 seconds.
2. It manually triggers the 20-second job while other jobs are running.
3. It waits another five seconds.
4. It replaces the 20-second job's pending schedule with three new occurrences.

The driver uses `Thread.sleep()` only to keep the demo process alive. It is not scheduler logic.

## 15. Interview scope

The minimum implementation for a 45-60 minute interview is:

```text
JobTask
Job
SchedulePolicy
Delayed trigger
DelayQueue
One scheduler consumer
ExecutorService worker pool
One-time and basic recurring scheduling
```

Manual execution, schedule updates, cancellation races, execution storage, and graceful shutdown are follow-ups.

For a distributed production system, discuss:

- Durable database storage.
- Atomic claiming by multiple scheduler nodes.
- Leases and heartbeats.
- At-least-once execution.
- Idempotent jobs.
- Retries and exponential backoff.
- Misfire handling.
- Metrics and dead-letter handling.

## 16. Deep flow: Producer -> DelayQueue -> Consumer -> Worker Pool

### Step 1: Create the job

```java
String jobId = jobService.createJob(() -> sendEmail());
```

The task is stored inside `Job`; it does not execute yet.

### Step 2: Produce the first trigger

```java
schedulerService.createSchedule(
        jobId,
        new FixedRateSchedulePolicy(firstTime, interval, 5)
);
```

The service creates a schedule and its first trigger:

```text
Scheduler API = producer
DelayQueue     = buffer
```

### Step 3: Wait for time

The scheduler consumer blocks:

```java
JobExecutionTrigger trigger = triggerQueue.take();
```

The trigger may already be inside the queue, but `take()` cannot return it before `triggerAt`.

### Step 4: Consume and claim

When the trigger becomes due:

```java
if (!trigger.claim()) {
    return;
}
```

This prevents cancelled or duplicate processing.

### Step 5: Produce the next recurrence

For a recurring schedule, the consumer also becomes a producer:

```text
consume occurrence 1
    -> enqueue occurrence 2
```

For one-time, manual, or final occurrences, no next trigger is produced.

### Step 6: Create an execution

```java
JobExecution execution =
        new JobExecution(IdGenerator.generate("E"), trigger.jobId());
```

The trigger represents the scheduling request. The execution represents the runtime attempt.

### Step 7: Dispatch to workers

```java
workerPool.submit(() -> jobWorker.execute(execution));
```

This creates a second producer-consumer stage:

```text
Scheduler thread        = producer of worker tasks
Executor internal queue = buffer
Worker threads          = consumers
```

### Step 8: Execute the task

`JobWorker`:

```text
load Job
    -> mark execution RUNNING
    -> call JobTask.execute()
    -> mark COMPLETED or FAILED
```

The complete thread flow is:

```text
Main/client thread
    -> produces Trigger

DelayQueue
    -> waits until due

Scheduler thread
    -> claims Trigger
    -> creates next Trigger
    -> creates JobExecution
    -> submits worker task

ExecutorService
    -> queues worker task

Worker thread
    -> JobWorker.execute()
    -> JobTask.execute()
    -> COMPLETED or FAILED
```

The scheduler must not execute business logic itself. If it ran a 40-second task, it could not consume other due triggers during those 40 seconds.

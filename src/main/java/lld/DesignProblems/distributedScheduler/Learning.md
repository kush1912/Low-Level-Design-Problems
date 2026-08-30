1. Should Job be a record?
   - No. A job owns a changing collection of schedules, so it is not an immutable value object.
   - The executable task and ID remain immutable, while schedules can be added or removed.

2. How are IDs generated in the demo?
   - One `IdGenerator` creates string IDs using a type prefix and a random six-digit number: `J-123456`, `S-234567`, `T-345678`, and `E-456789`.
   - `IdGenerator` should not be a Singleton because it has no instance state. It is a stateless utility class with a private constructor and a static `generate()` method, so creating and managing one shared object would add no value.
   - `ThreadLocalRandom.current()` already provides thread-safe random-number generation without requiring shared mutable state inside `IdGenerator`.
   - A Singleton or injected generator service would become useful only if ID generation later required shared state or replaceable behavior, such as a sequence counter or a database-backed generator.
   - This keeps the interview implementation small. A production system should use UUIDs, database-generated IDs, or another collision-resistant strategy.

3. Why is JobTask an interface?
   - It separates business behavior from scheduling, so the scheduler can execute email, payment, report, or other tasks through the same `execute()` contract.
   - There is no common state requiring an abstract class. As a functional interface, it also supports lambda-based tasks.
   - Example:
     ```java
     JobTask emailTask = () -> emailService.sendEmail("user@example.com");
     JobTask paymentTask = () -> paymentService.processPayment("payment-123");

     Job emailJob = new Job(IdGenerator.generate("J"), emailTask);
     Job paymentJob = new Job(IdGenerator.generate("J"), paymentTask);

     job.task().execute();
     ```

4. Why does JobExecution own the status instead of Job?
   - A `Job` describes reusable work and does not execute only once. Each occurrence has its own result.
   - A recurring job can have one completed execution, one failed execution, and another queued execution.
     ```text
     Job: Generate daily report

     Monday execution:    COMPLETED
     Tuesday execution:   FAILED
     Wednesday execution: QUEUED
     ```
   - `ExecutionStatus` contains `QUEUED`, `RUNNING`, `COMPLETED`, `FAILED`, and `CANCELLED`.
   - It is stored in an `AtomicReference` because scheduler and update threads may concurrently attempt transitions such as `QUEUED -> RUNNING` and `QUEUED -> CANCELLED`.
   - `compareAndSet()` makes the validation and update atomic. Unlike `volatile`, it prevents check-then-act race conditions.

5. What is Instant and why do we use it?
   - `Instant` represents an exact point on the UTC timeline, making execution times unambiguous across machines and time zones.
   - It is commonly used in production for scheduling, event timestamps, retries, expirations, and audit records.
   - User-local time can be converted to an `Instant` before scheduling.

6. What is the difference between Schedule and SchedulePolicy?
   - `Schedule` is an identifiable entity connecting a `Job` with a timing policy.
     ```text
     Schedule
      |- ScheduleId
      |- JobId
      `- SchedulePolicy
     ```
   - `SchedulePolicy` contains the algorithm for calculating execution times.
     ```java
     public interface SchedulePolicy {
         Instant firstExecutionAt();

         Optional<Instant> nextExecutionAfter(
             Instant scheduledAt,
             int currentOccurrence
         );
     }
     ```
   - One job can have multiple schedules:
     ```text
     Report Job
      |- Schedule A: every day at 6 AM
      `- Schedule B: every day at 4 PM
     ```
   - Each schedule has an ID so that it can be updated or cancelled independently.

7. Why is SchedulePolicy an interface, and why are its implementations records?
   - Different policies calculate their next execution differently. A one-time policy has no next occurrence, while a fixed-rate policy adds an interval.
   - The interface keeps scheduler logic independent of concrete policies and allows future strategies such as fixed-delay, daily, weekly, or cron.
   - `OneTimeSchedulePolicy` stores one immutable execution time:
     ```java
     SchedulePolicy oneTime = new OneTimeSchedulePolicy(
         Instant.parse("2026-08-30T10:00:00Z")
     );
     ```
   - `FixedRateSchedulePolicy` stores an immutable first execution time, interval, and maximum execution count:
     ```java
     SchedulePolicy recurring = new FixedRateSchedulePolicy(
         Instant.parse("2026-08-30T10:00:00Z"),
         Duration.ofMinutes(5),
         5
     );
     ```
   - Each scheduled trigger carries its occurrence number. After occurrence five is claimed, the policy returns `Optional.empty()` instead of producing another trigger.
   - Manual triggers use occurrence zero and do not affect a recurring schedule's execution count.
   - Records are appropriate because policy configurations are immutable values. They provide final fields, constructors, accessors, value equality, `hashCode()`, and `toString()`.
   - An immutable final class would also be correct and may be preferred for framework compatibility or custom construction.
   - Updating a schedule replaces its complete policy:
     ```java
     schedule.updatePolicy(
         new FixedRateSchedulePolicy(
             newFirstExecution,
             Duration.ofMinutes(10),
             5
         )
     );
     ```
   - `OneTimeSchedulePolicy` explicitly implements `firstExecutionAt()` because its generated record accessor is named `executeAt()`.
   - `FixedRateSchedulePolicy` could rely on its generated `firstExecutionAt()` accessor, but it is implemented explicitly for clarity.
   - A one-time policy returns `Optional.empty()` from `nextExecutionAfter()` because the absence of another occurrence is a valid outcome, not an error.

8. What is Delayed and why does JobExecutionTrigger implement it?
   - `Delayed` is an interface from `java.util.concurrent` representing an object that becomes available only after its delay expires.
     ```java
     public interface Delayed extends Comparable<Delayed> {
         long getDelay(TimeUnit unit);
     }
     ```
   - `getDelay()` returns how much time remains. A zero or negative value means the trigger is due.
     ```java
     @Override
     public long getDelay(TimeUnit unit) {
         long remainingMillis =
             triggerAt.toEpochMilli() - System.currentTimeMillis();

         return unit.convert(
             remainingMillis,
             TimeUnit.MILLISECONDS
         );
     }
     ```
   - `Delayed` extends `Comparable` because the queue must keep the earliest trigger at the front. Sequence provides stable ordering when times are equal.
     ```java
     @Override
     public int compareTo(Delayed other) {
         JobExecutionTrigger trigger = (JobExecutionTrigger) other;

         int result = triggerAt.compareTo(trigger.triggerAt());
         return result != 0
             ? result
             : Long.compare(sequence, trigger.sequence());
     }
     ```
   - `Delayed` does not execute a task, create a thread, or sleep. It only exposes timing and ordering information.

9. What is the difference between Delayed and DelayQueue?
   - `Delayed` is implemented by one item and describes when that item becomes available.
   - `DelayQueue` is the thread-safe container that stores multiple delayed items, keeps the earliest one at the front, and blocks until it is due.
     ```java
     JobExecutionTrigger trigger = createTrigger();

     DelayQueue<JobExecutionTrigger> queue = new DelayQueue<>();
     queue.put(trigger);

     // Blocks until trigger.getDelay(...) is zero or negative.
     JobExecutionTrigger dueTrigger = queue.take();
     ```
   - In this scheduler, `JobExecutionTrigger` implements `Delayed`, while `JobExecutionTriggerQueue` owns the `DelayQueue`.
   - Once claimed, a trigger creates a separate `JobExecution`. The execution itself has no scheduling information.

10. How is DelayQueue related to PriorityQueue?
    - `DelayQueue` can be understood as a thread-safe priority queue specialized for delayed elements.
    - It is not a subclass of `PriorityQueue`, but it uses priority-based ordering internally.

    | Structure | Ordered | Thread-safe | Waits until due |
    |---|---:|---:|---:|
    | `PriorityQueue` | Yes | No | No |
    | `PriorityBlockingQueue` | Yes | Yes | No |
    | `DelayQueue` | Yes | Yes | Yes |

    ```text
    Current time: 10:00
    Job A: 10:10
    Job B: 10:02
    Job C: 10:05

    Queue order: Job B -> Job C -> Job A

    PriorityQueue.poll()          -> returns Job B immediately
    PriorityBlockingQueue.take() -> returns Job B immediately
    DelayQueue.take()            -> waits until 10:02
    ```

    - `PriorityBlockingQueue.take()` blocks only when the queue is empty.
    - `DelayQueue.take()` also blocks when its earliest element exists but is not due.

11. Final mental model
    ```text
    Job
     |- JobId
     |- JobTask
     `- Multiple Schedules

    Schedule
     |- ScheduleId
     |- JobId
     `- SchedulePolicy

    JobExecutionTrigger
     |- TriggerId
     |- JobId
     |- Optional ScheduleId
     |- TriggerType
     |- triggerAt
     `- TriggerStatus

    JobExecution
     |- ExecutionId
     |- JobId
     `- ExecutionStatus
    ```

    ```text
    One Job -> many Schedules
    Schedule or manual request -> JobExecutionTrigger
    Claimed JobExecutionTrigger -> one JobExecution
    One JobExecution -> executes one Job once
    ```

12. Manual execution and schedule updates
    - `runNow(jobId)` creates an immediately due manual trigger. It has no schedule ID, is not stored as a pending scheduled trigger, and does not create another occurrence.
    - A manual execution does not change or increment any recurring schedule associated with the same job.
    - `updateSchedule(jobId, scheduleId, newPolicy)` updates the policy, cancels the currently pending trigger, and enqueues a replacement starting at the new policy's first execution time.
    - A running execution is not interrupted by a schedule update because it has already been created from a claimed trigger.
    - Trigger replacement and recurring-trigger creation synchronize on the job. Conditional repository removal prevents an already-claimed old trigger from creating a duplicate next occurrence after an update.

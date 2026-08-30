# Distributed Scheduler Follow-up Questions

## F1. How would you scale the current scheduler from three workers to 10,000 or one million scheduled executions?

### Model answer

> Currently, the scheduler has three worker threads inside one JVM. If 10,000 jobs become due together, only three execute while the remaining jobs wait in the executor queue.
>
> I would first optimize within one JVM by classifying the workload. For CPU-bound jobs, I would use a bounded platform-thread pool sized close to the number of CPU cores. For blocking I/O-bound jobs, I could use Java virtual threads to support much higher concurrency without creating the same number of operating-system threads. I would still use rate limits or semaphores because virtual threads do not increase database connections or downstream-service capacity.
>
> Once a single JVM reaches its limit, I would separate scheduling from execution. Schedules and their next execution times would be persisted in a database. Multiple scheduler instances would atomically claim batches of due schedules and publish execution requests to a distributed queue such as Kafka or Service Bus.
>
> For one million schedules, I would partition them using something like `hash(scheduleId) % partitionCount`. Each scheduler instance would own or lease different partitions, allowing due schedules to be discovered in parallel without duplicate processing. The execution queue could also be partitioned, with the same job routed to the same partition when ordering is required.
>
> Finally, a horizontally scalable worker fleet would consume from the queue and autoscale based on queue depth and processing delay. The required worker capacity would depend on job duration, CPU or I/O characteristics, downstream capacity, and the completion-time SLA.

## F2. If this scheduler were implemented in a real production system, what would replace each in-memory component?

### Model answer

> The domain concepts—`Job`, `Schedule`, `Trigger`, and `JobExecution`—would remain mostly the same. I would replace the in-memory storage, coordination, queueing, and execution infrastructure with durable distributed components.
>
> The in-memory repositories would become database tables for jobs, schedules, pending triggers, and execution history. A job record would store a serializable job type such as `SEND_EMAIL` and a JSON payload containing inputs such as the recipient and template ID. I would not persist a Java `JobTask` lambda because executable objects and their dependencies cannot be reliably transferred between machines.
>
> Worker services would contain the actual executable code and maintain a handler registry such as `SEND_EMAIL -> SendEmailJobHandler`. When a worker receives an execution message, it uses the job type to select the handler and passes the deserialized payload to it. This keeps data in the database and broker while executable behavior remains deployed as application code.
>
> The database would be the source of truth for scheduling, with an index on status and `nextExecutionAt`. I would run multiple scheduler nodes, where each node means one independently running scheduler process, container, Kubernetes pod, or VM instance. Scheduler nodes only discover and claim due triggers; worker nodes perform the actual business work.
>
> The scheduler nodes would atomically claim batches of due triggers using transactions, conditional updates, or leases. They could own different schedule partitions for scalability, and another node could acquire a failed node's partitions after its lease expires. Database-level claiming would replace JVM-only coordination such as `synchronized` and `AtomicReference`.
>
> After claiming a trigger, the scheduler would create an execution record and publish an execution message to a durable broker such as Kafka or Service Bus. The local `ExecutorService` would be replaced by a horizontally scalable fleet of stateless workers consuming from that broker.
>
> Within each worker instance, I could use a bounded platform-thread pool for CPU-bound jobs or virtual threads for blocking I/O jobs. I would still apply concurrency limits and rate limiting because additional threads do not increase database connections or downstream-service capacity.
>
> The system would normally provide at-least-once execution, so every message would contain an execution ID used as an idempotency key. I would also add retries with exponential backoff, dead-letter handling, centralized logging, metrics, tracing, partition ownership, and lease recovery when a scheduler or worker node fails.

## F3. How would multiple scheduler nodes prevent duplicate job execution?

### Model answer

> When we run multiple scheduler nodes, more than one node may query the database and find the same due trigger. To prevent both nodes from processing it, I would use a database-backed lease.
>
> Each trigger would have fields such as `status`, `leaseOwner`, and `leaseExpiry`. Initially, the trigger is in the `PENDING` state with no lease owner. When a scheduler finds it, the scheduler tries to atomically update the trigger from `PENDING` to `CLAIMED`, records its node ID as the lease owner, and sets a short lease expiry time.
>
> The update would include a condition that the trigger must still be pending or that its previous lease has expired. If two scheduler nodes attempt to claim the same trigger at the same time, only one conditional update succeeds. The successful node receives an affected-row count of one and processes the trigger. The other node receives zero and skips it.
>
> Once it owns the lease, the scheduler creates an execution record and publishes the execution request to the worker queue. If this processing takes longer than expected, the scheduler can periodically renew the lease using a heartbeat.
>
> The main benefit of using an expiry is failure recovery. Suppose Scheduler A claims a trigger and crashes before publishing it. The trigger remains claimed temporarily, but once the lease expires, Scheduler B can reclaim it. Without lease expiry, the trigger could remain stuck forever.
>
> There is still a possibility that Scheduler A publishes the message and crashes before marking the trigger complete. After the lease expires, Scheduler B may publish it again. Therefore, lease-based claiming gives us at-least-once processing rather than perfect exactly-once execution.
>
> To handle that, every execution request carries a unique execution ID. Workers use that ID as an idempotency key so processing the same message more than once does not repeat the business side effect.

## F4. What happens if a worker crashes while executing a job?

## F5. How would retries, exponential backoff, and dead-letter handling work?

## F6. What happens when a job takes longer than its recurrence interval?

## F7. How would schedule updates and cancellation work across multiple scheduler nodes?

## F8. How would you monitor the scheduler and detect delayed, failed, or stuck executions?

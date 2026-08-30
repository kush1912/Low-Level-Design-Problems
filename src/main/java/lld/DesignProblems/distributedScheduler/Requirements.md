# Distributed Scheduler

## Functional Requirements

1. Schedule a one-time job for execution at a specified time.
2. Schedule a recurring job at a fixed interval.
3. Trigger a job immediately without changing or cancelling its existing schedules.
4. Update the schedule for future executions without interrupting a currently running execution.
5. Cancel a scheduled job.
6. Execute independent jobs concurrently using a configurable worker pool.
7. Track the current status of each job execution.
8. Isolate job failures so that one failed job does not stop the scheduler or other jobs.
9. Support graceful shutdown and reject new jobs after shutdown begins.

## Time Permits Extensions

1. Retry failed jobs using a configurable retry policy.
2. Pause and resume scheduled jobs.
3. Support fixed-delay recurring jobs.
4. Support priorities for jobs scheduled at the same time.
5. Configure whether recurring executions may overlap.
6. Expose execution history for completed and failed attempts.

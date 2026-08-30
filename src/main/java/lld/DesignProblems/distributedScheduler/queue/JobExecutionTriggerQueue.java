package lld.DesignProblems.distributedScheduler.queue;

import lld.DesignProblems.distributedScheduler.models.JobExecutionTrigger;

import java.util.concurrent.DelayQueue;

public final class JobExecutionTriggerQueue {
    private final DelayQueue<JobExecutionTrigger> queue = new DelayQueue<>();

    public void add(JobExecutionTrigger trigger) {
        queue.put(trigger);
    }

    public JobExecutionTrigger take() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }
}

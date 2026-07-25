package lld.rateLimiter.model;

import lld.rateLimiter.interfaces.RateLimiter;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Decorator that adds QPS / throughput metrics on top of ANY RateLimiter.
 *
 * It implements RateLimiter (so it is a drop-in replacement) and also HAS-A
 * RateLimiter (the real algorithm it delegates to). This is the Decorator
 * pattern: behaviour (metrics) is added without touching the algorithm class,
 * and it works for Fixed Window, Sliding Window, Token Bucket, etc.
 */
public class MetricsRateLimiter implements RateLimiter {

    private final RateLimiter delegate;           // the wrapped algorithm
    private final long startTimeMillis;

    // Lifetime counters (thread-safe without locking).
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong allowedRequests = new AtomicLong(0);
    private final AtomicLong rejectedRequests = new AtomicLong(0);

    // Timestamps of ALLOWED requests in the last second, for a rolling QPS.
    private final ConcurrentLinkedDeque<Long> recentAllowed = new ConcurrentLinkedDeque<>();

    public MetricsRateLimiter(RateLimiter delegate) {
        this.delegate = delegate;
        this.startTimeMillis = System.currentTimeMillis();
    }

    @Override
    public boolean allowRequest(String clientId) {
        totalRequests.incrementAndGet();
        boolean allowed = delegate.allowRequest(clientId);
        if (allowed) {
            allowedRequests.incrementAndGet();
            recentAllowed.addLast(System.currentTimeMillis());
        } else {
            rejectedRequests.incrementAndGet();
        }
        return allowed;
    }

    // Lifetime throughput: allowed requests per second since creation.
    public double getAllowedQPS() {
        return perSecond(allowedRequests.get());
    }

    // Lifetime offered load: all requests per second since creation.
    public double getIncomingQPS() {
        return perSecond(totalRequests.get());
    }

    // Rolling QPS: number of ALLOWED requests in the last 1000 ms.
    public long getCurrentQPS() {
        long cutoff = System.currentTimeMillis() - 1000;
        // Drop timestamps older than 1 second from the front.
        Long head;
        while ((head = recentAllowed.peekFirst()) != null && head < cutoff) {
            recentAllowed.pollFirst();
        }
        return recentAllowed.size();
    }

    public long getTotalRequests()    { return totalRequests.get(); }
    public long getAllowedRequests()  { return allowedRequests.get(); }
    public long getRejectedRequests() { return rejectedRequests.get(); }

    private double perSecond(long count) {
        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        if (elapsedMillis <= 0) return 0.0;
        return count / (elapsedMillis / 1000.0);
    }

    public void printStats() {
        System.out.printf(
                "Stats -> total=%d, allowed=%d, rejected=%d | incomingQPS=%.2f, allowedQPS=%.2f, currentQPS(last 1s)=%d%n",
                getTotalRequests(), getAllowedRequests(), getRejectedRequests(),
                getIncomingQPS(), getAllowedQPS(), getCurrentQPS());
    }
}

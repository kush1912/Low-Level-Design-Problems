package lld.rateLimiter.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DriverCode {

    public static void main(String[] args) throws InterruptedException {
        multiClientConcurrencyTest();
    }

    // Multi-client concurrency test: many threads hammer SEVERAL clients at once.
    // Verifies both properties together: (a) thread-safety (no client exceeds
    // its limit) and (b) isolation (each client is counted independently).
    private static void multiClientConcurrencyTest() throws InterruptedException {
        System.out.println("=== Multi-client concurrency test: max 100 / 1000 ms, 5 clients ===");
        int maxRequests = 100;
        int clients = 5;
        int requestsPerClient = 1000;
        // Wrap the real limiter in the MetricsRateLimiter decorator so we also
        // collect QPS/throughput stats while the concurrency test runs.
        MetricsRateLimiter limiter =
                new MetricsRateLimiter(new FixedWindowRateLimiter(maxRequests, 1000));

        // One counter per client to tally how many each was allowed.
        // Initialize BEFORE submitting so tasks never hit a null counter.
        ConcurrentHashMap<String, AtomicInteger> allowedPerClient = new ConcurrentHashMap<>();
        for (int c = 0; c < clients; c++) {
            allowedPerClient.put("client-" + c, new AtomicInteger(0));
        }

        // Global order in which requests are ACCEPTED across all threads/clients.
        AtomicInteger acceptanceOrder = new AtomicInteger(0);
        ExecutorService pool = Executors.newFixedThreadPool(50);

        // Interleaved (round-robin) submission: client-0, client-1, ... then repeat.
        // Spreads contention evenly across all clients from the start.
        for (int i = 0; i < requestsPerClient; i++) {
            for (int c = 0; c < clients; c++) {
                String clientId = "client-" + c;
                pool.submit(() -> {
                    if (limiter.allowRequest(clientId)) {
                        int order = acceptanceOrder.incrementAndGet();
                        allowedPerClient.get(clientId).incrementAndGet();
                        // Show acceptance order, which client, and the handling thread.
                        System.out.println("[accepted #" + order + "] " + clientId
                                + " -> processing on " + Thread.currentThread().getName());
                    }
                });
            }
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        // ---- Summary ----
        System.out.println("\n===== SUMMARY =====");
        int totalAllowed = 0;
        boolean pass = true;
        for (int c = 0; c < clients; c++) {
            String clientId = "client-" + c;
            int allowed = allowedPerClient.get(clientId).get();
            totalAllowed += allowed;
            System.out.println(clientId + " allowed: " + allowed + " / " + maxRequests);
            if (allowed != maxRequests) pass = false; // expect EXACTLY the limit each
        }
        System.out.println("Total requests fired : " + (requestsPerClient * clients));
        System.out.println("Total accepted       : " + totalAllowed
                + " (expected " + (maxRequests * clients) + ")");
        System.out.println(pass
                ? "PASS: every client independently capped at exactly " + maxRequests + "."
                : "FAIL: a client's count was off -> race or isolation bug!");

        // QPS / throughput metrics gathered by the decorator during this run.
        limiter.printStats();
    }
}

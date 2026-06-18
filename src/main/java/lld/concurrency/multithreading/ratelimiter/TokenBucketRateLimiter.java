package lld.concurrency.multithreading.ratelimiter;

import java.util.concurrent.locks.ReentrantLock;

/*
 * Token Bucket Rate Limiter
 *
 * Concept: Imagine a bucket that holds tokens (permissions).
 * - Bucket has a MAX capacity (e.g., 10 tokens)
 * - Tokens are added at a fixed rate (e.g., 1 token per second)
 * - Each request consumes 1 token
 * - No tokens available → request REJECTED
 *
 * Why thread-safe? Multiple threads (users/requests) will call tryConsume()
 * concurrently — we must protect the shared token count.
 *
 *  Real-world example: APIs allow 10 requests/second. Each request takes a token. If you exceed 10, you get HTTP 429 (Too Many Requests).
 * 
 */
public class TokenBucketRateLimiter {

    private final int maxTokens;       // Maximum tokens the bucket can hold
    private final int refillRate;      // Tokens added per second
    private double currentTokens;      // Current available tokens (double for partial token calculation)
    private long lastRefillTimestamp;   // Last time we refilled tokens

    // ReentrantLock to protect currentTokens and lastRefillTimestamp
    // These are shared mutable state accessed by multiple threads
    private final ReentrantLock lock = new ReentrantLock();

    public TokenBucketRateLimiter(int maxTokens, int refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.currentTokens = maxTokens; // Start with a full bucket
        this.lastRefillTimestamp = System.nanoTime();
    }

    /*
     * Lazy Refill: Instead of a background thread adding tokens every second,
     * we calculate how many tokens SHOULD have been added since the last call.
     *
     * Why lazy? No extra thread needed, no scheduling overhead.
     * We just do the math when someone actually tries to consume a token.
     *
     * Formula: tokensToAdd = refillRate × timeSinceLastRefill (in seconds)
     *
     * Example: refillRate=5, last refill was 2 seconds ago
     *   tokensToAdd = 5 × 2.0 = 10.0
     *   currentTokens = min(currentTokens + 10.0, maxTokens)  ← cap at max
     *
     * NOTE: This method is always called INSIDE the lock, so no extra sync needed.
     */
    private void refill() {
        long now = System.nanoTime();
        // Convert nanoseconds to seconds (1 second = 1_000_000_000 nanoseconds)
        double elapsedSeconds = (now - lastRefillTimestamp) / 1_000_000_000.0;
        double tokensToAdd = elapsedSeconds * refillRate;

        // Add tokens but don't exceed bucket capacity
        currentTokens = Math.min(currentTokens + tokensToAdd, maxTokens);
        lastRefillTimestamp = now;
    }

    /*
     * tryConsume() — the core method called by every incoming request.
     *
     * Flow:
     * 1. Acquire lock (only one thread checks/modifies tokens at a time)
     * 2. Refill tokens based on elapsed time (lazy refill)
     * 3. Check: do we have enough tokens?
     *    - YES → consume (subtract tokens), return true (ALLOWED)
     *    - NO  → return false (REJECTED / HTTP 429)
     * 4. Release lock in finally (guaranteed even if exception occurs)
     *
     * Why tryLock() is NOT used here:
     * We use lock.lock() (blocking) because every request MUST get a definitive
     * answer (allowed/rejected). tryLock() would skip the check entirely.
     *
     * @param tokens — number of tokens to consume (usually 1 per request)
     * @return true if request is allowed, false if rate limited
     */
    public boolean tryConsume(int tokens) {
        lock.lock();
        try {
            refill();  // Step 1: Calculate tokens accumulated since last call

            if (currentTokens >= tokens) {
                // Enough tokens available → allow the request
                currentTokens -= tokens;
                return true;
            }

            // Not enough tokens → reject (rate limited)
            return false;
        } finally {
            lock.unlock(); // Always release lock, even if exception is thrown
        }
    }

    /*
     * Simulation: 10 threads (users) sending requests to an API
     * Rate limiter allows max 5 tokens, refills 1 token/sec
     *
     * Expected behavior:
     * - First 5 requests → ALLOWED (bucket starts full with 5 tokens)
     * - Remaining requests → REJECTED (bucket empty, refill is too slow)
     * - After waiting 1 second → some requests ALLOWED again (tokens refilled)
     */
    public static void main(String[] args) throws InterruptedException {
        // Max 5 tokens, refill 1 token per second
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(5, 1);

        System.out.println("=== Burst: 10 requests at once (only 5 tokens available) ===");

        // Create 10 threads simulating concurrent API requests
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int requestId = i + 1;
            threads[i] = new Thread(() -> {
                // Each request tries to consume 1 token
                boolean allowed = rateLimiter.tryConsume(1);
                System.out.println("Request " + requestId + " -> " + (allowed ? "[ALLOWED]" : "[REJECTED]"));
            });
            threads[i].start();
        }


        // Wait for all threads to finish
        for (Thread t : threads) {
            t.join();
        }

        // Wait 3 seconds — tokens should refill (1 token/sec × 3 sec = 3 new tokens)
        System.out.println("\nWaiting 3 seconds for tokens to refill...\n");
        Thread.sleep(3000);

        System.out.println("=== After refill: 3 more requests ===");
        for (int i = 0; i < 3; i++) {
            final int requestId = i + 11;
            boolean allowed = rateLimiter.tryConsume(1);
            System.out.println("Request " + requestId + " -> " + (allowed ? "[ALLOWED]" : "[REJECTED]"));
        }

        System.out.println("\nDone!");
    }
}

/*
 * =====================================================================
 * WHY IS THE MAIN METHOD DESIGNED THIS WAY?
 * =====================================================================
 *
 * The rate limiter has 2 features to test:
 *   1. Limit requests — reject when too many come at once
 *   2. Refill tokens over time — allow again after waiting
 *
 * We test them separately:
 *
 * TEST 1 (multi-threaded): Can 10 users call the API at the exact same moment?
 *   → 10 threads race for 5 tokens → only 5 allowed, 5 rejected
 *   → PROVES: thread-safety and limiting works
 *
 * TEST 2 (main thread only): After waiting, do tokens come back?
 *   → Wait 3 seconds, try 3 requests on main thread → all allowed
 *   → PROVES: lazy refill works
 *
 * Why no threads in Test 2?
 *   Because we don't need them. We're not testing concurrency again.
 *   We're just checking: "did tokens come back after 3 seconds?"
 *
 *   Think of it like this:
 *   - Test 1: 10 people rush a door at once → only 5 get in
 *   - Test 2: Wait 3 minutes, then ONE person walks to the door → gets in
 *   You don't need 10 people again just to check if the door reopened.
 *
 *   We COULD use threads for Test 2 — it would still work.
 *   We just didn't need to. One person is enough.
 * =====================================================================
 */

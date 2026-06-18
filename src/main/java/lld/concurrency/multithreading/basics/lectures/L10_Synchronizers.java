package lld.concurrency.multithreading.basics.lectures;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.BrokenBarrierException;

/*
 * =====================================================================
 * LESSON 10: SYNCHRONIZERS — Semaphore, CountDownLatch, CyclicBarrier
 * =====================================================================
 *
 * These are higher-level coordination tools from java.util.concurrent.
 * Instead of writing wait/notify/lock yourself, use these ready-made tools.
 *
 * | Tool            | Analogy                           | Reusable? |
 * |-----------------|-----------------------------------|-----------|
 * | Semaphore       | Parking lot with N spots          | YES       |
 * | CountDownLatch  | Rocket launch countdown (3,2,1,GO)| NO        |
 * | CyclicBarrier   | Friends meeting at a restaurant   | YES       |
 * =====================================================================
 */
public class L10_Synchronizers {

    // =====================================================================
    // DEMO 1: SEMAPHORE — Control access to limited resources
    // =====================================================================
    // A semaphore holds N "permits". Threads acquire a permit to access
    // the resource. If no permits available, thread waits.
    //
    // Analogy: Parking lot with 3 spots.
    //   Car arrives → spot available? → park (acquire permit)
    //   Lot full → wait in line
    //   Car leaves → frees a spot (release permit) → next car can enter
    //
    // Use cases: connection pools, rate limiting, resource pools

    static void demoSemaphore() throws InterruptedException {
        // Only 3 threads can access the resource simultaneously
        Semaphore parkingLot = new Semaphore(3);
        // Semaphore(3) → 3 permits available
        // Semaphore(3, true) → fair (FIFO order)

        Thread[] cars = new Thread[6]; // 6 cars, 3 spots
        for (int i = 0; i < 6; i++) {
            final int carId = i + 1;
            cars[i] = new Thread(() -> {
                try {
                    System.out.println("  Car " + carId + ": Arriving...");
                    parkingLot.acquire(); // Wait for a permit (blocks if none available)
                    // acquire() decrements permits by 1. If 0 → blocks until release.
                    System.out.println("  Car " + carId + ": PARKED (spots left: " + parkingLot.availablePermits() + ")");
                    Thread.sleep(200); // Simulate being parked
                    parkingLot.release(); // Return the permit
                    // release() increments permits by 1. Wakes up a waiting thread.
                    System.out.println("  Car " + carId + ": LEFT");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            cars[i].start();
            Thread.sleep(50); // Stagger arrivals
        }
        for (Thread t : cars) t.join();

        /*
         * Other Semaphore methods:
         *   tryAcquire()          → non-blocking: returns false if no permit
         *   tryAcquire(time,unit) → wait up to timeout for a permit
         *   acquire(n)            → acquire N permits at once
         *   release(n)            → release N permits at once
         *   availablePermits()    → how many permits are free
         *
         * Semaphore(1) = mutex (like a ReentrantLock, but NOT reentrant!)
         */
    }

    // =====================================================================
    // DEMO 2: COUNTDOWN LATCH — Wait for N events to complete
    // =====================================================================
    // A latch starts with a count. Threads call countDown() to decrement.
    // Other threads call await() to block until count reaches 0.
    //
    // Analogy: Rocket launch countdown.
    //   3 systems must report "ready": fuel, navigation, engines.
    //   Launch controller waits until all 3 report ready (count → 0).
    //   Then launches the rocket.
    //
    // KEY: CountDownLatch is ONE-TIME USE. Once count reaches 0, it stays at 0.
    //      Cannot be reset. For reusable version, use CyclicBarrier.

    static void demoCountDownLatch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3); // Wait for 3 events

        // 3 service threads, each taking different time
        String[] services = {"Database", "Cache", "MessageQueue"};
        int[] startupTimes = {300, 200, 100};

        System.out.println("  Application starting...");

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            new Thread(() -> {
                try {
                    Thread.sleep(startupTimes[idx]); // Simulate startup
                    System.out.println("  " + services[idx] + " is ready!");
                    latch.countDown(); // Decrement count by 1
                    // countDown() is thread-safe, no lock needed
                    // count goes: 3 → 2 → 1 → 0
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        // Main thread waits until all services are ready
        latch.await(); // Blocks until count == 0
        // await(timeout, unit) → waits with timeout, returns false if expired
        System.out.println("  ALL services ready! Application started.");

        // latch.countDown() after count=0 has NO effect (stays at 0)
        // latch.await() after count=0 returns IMMEDIATELY (doesn't block)
        // This is why it's one-time use — can't reset back to 3
    }

    // =====================================================================
    // DEMO 3: CYCLIC BARRIER — All threads wait for each other
    // =====================================================================
    // All N threads must reach the barrier before ANY can proceed.
    //
    // Analogy: 3 friends going to a restaurant.
    //   First to arrive waits. Second arrives, waits.
    //   Third arrives — NOW all 3 enter together.
    //
    // "Cyclic" = can be REUSED after all threads pass the barrier.
    //
    // Key difference from CountDownLatch:
    //   Latch: some threads count down, OTHER threads await
    //   Barrier: ALL threads both arrive AND wait

    static void demoCyclicBarrier() throws InterruptedException {
        // 3 threads must reach barrier. When all arrive, run the barrier action.
        CyclicBarrier barrier = new CyclicBarrier(3, () -> {
            // This Runnable runs when ALL threads reach the barrier
            // Runs on the LAST thread to arrive
            System.out.println("  --- All threads arrived! Barrier opened. ---");
        });

        String[] friends = {"Alice", "Bob", "Charlie"};
        int[] travelTimes = {100, 300, 200};

        Thread[] threads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                try {
                    System.out.println("  " + friends[idx] + ": Traveling to restaurant...");
                    Thread.sleep(travelTimes[idx]); // Simulate travel
                    System.out.println("  " + friends[idx] + ": Arrived! Waiting for others...");
                    barrier.await(); // Wait for all 3 to arrive
                    // await() blocks until all N threads call await()
                    // Then ALL threads proceed simultaneously
                    System.out.println("  " + friends[idx] + ": Let's eat!");
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        // CyclicBarrier can be REUSED (unlike CountDownLatch)
        System.out.println("\n  --- Round 2: Dessert! ---");
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                try {
                    System.out.println("  " + friends[idx] + ": Choosing dessert...");
                    Thread.sleep(100);
                    barrier.await(); // Same barrier, reused!
                    System.out.println("  " + friends[idx] + ": Dessert time!");
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();
    }

    // =====================================================================
    // DEMO 4: Comparison — When to use what?
    // =====================================================================
    /*
     * | Feature              | Semaphore      | CountDownLatch  | CyclicBarrier   |
     * |----------------------|----------------|-----------------|-----------------|
     * | Purpose              | Limit access   | Wait for events | Sync at a point |
     * | Permits/Count        | N permits      | Count down to 0 | N parties       |
     * | Who waits?           | Acquirers      | Awaiter(s)      | All parties     |
     * | Who signals?         | Releasers      | Count-downers   | Last to arrive  |
     * | Reusable?            | YES            | NO              | YES             |
     * | Threads pass through | One at a time  | All at once     | All at once     |
     *
     * Use Semaphore when:
     *   - Limiting concurrent access to a resource (pool of N)
     *   - Rate limiting (permits as tokens)
     *
     * Use CountDownLatch when:
     *   - Main thread waits for N worker threads to finish
     *   - Application startup: wait for all services to be ready
     *   - One-time event coordination
     *
     * Use CyclicBarrier when:
     *   - All threads must reach a point before any can proceed
     *   - Multi-phase computation (all finish phase 1, then start phase 2)
     *   - Need to reuse the synchronization point
     */

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== DEMO 1: Semaphore (Parking Lot) ===\n");
        demoSemaphore();

        System.out.println("\n=== DEMO 2: CountDownLatch (Service Startup) ===\n");
        demoCountDownLatch();

        System.out.println("\n=== DEMO 3: CyclicBarrier (Friends at Restaurant) ===\n");
        demoCyclicBarrier();

        System.out.println("\nDone!");
    }
}

/*
 * =====================================================================
 * SUMMARY
 * =====================================================================
 *
 * Semaphore:       "Only N at a time" (parking lot, connection pool)
 * CountDownLatch:  "Wait for N things to happen" (startup, one-time)
 * CyclicBarrier:   "Everyone wait for everyone" (phases, reusable)
 *
 * These are READY-MADE tools — don't reinvent with wait/notify!
 *
 * NEXT LESSON: L11_ThreadPools.java — Executors and thread pool management
 * =====================================================================
 */

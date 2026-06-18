package lld.concurrency.multithreading.basics.lectures;

/*
 * =====================================================================
 * LESSON 2: THREAD LIFECYCLE & IMPORTANT METHODS
 * =====================================================================
 *
 * A thread goes through these states (Thread.State enum):
 *
 *   NEW → RUNNABLE → RUNNING → (BLOCKED/WAITING/TIMED_WAITING) → TERMINATED
 *
 *   NEW:            Thread object created, start() not yet called
 *   RUNNABLE:       start() called, waiting for OS to schedule it
 *   RUNNING:        Actually executing on a CPU core
 *   BLOCKED:        Waiting to acquire a lock (synchronized)
 *   WAITING:        Waiting indefinitely (wait(), join() without timeout)
 *   TIMED_WAITING:  Waiting with a timeout (sleep(), join(timeout), wait(timeout))
 *   TERMINATED:     run() method completed or exception thrown
 *
 * Key methods covered:
 *   1. sleep()     — pause current thread for N milliseconds
 *   2. join()      — wait for another thread to finish
 *   3. interrupt() — signal a thread to stop (polite request, not forced kill)
 *   4. setDaemon() — mark thread as daemon (background thread)
 * =====================================================================
 */
public class L02_ThreadLifecycle {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== DEMO 1: Thread States ===\n");
        demoThreadStates();

        System.out.println("\n=== DEMO 2: sleep() ===\n");
        demoSleep();

        System.out.println("\n=== DEMO 3: join() ===\n");
        demoJoin();

        System.out.println("\n=== DEMO 4: interrupt() ===\n");
        demoInterrupt();

        System.out.println("\n=== DEMO 6: Retry Pattern with Threads ===\n");
        demoRetryPattern();

        System.out.println("\n=== DEMO 5: Daemon Threads ===\n");
        demoDaemon();

        Thread.sleep(500); // Let daemon demo finish printing

        System.out.println("\nAll demos done!");
    }

    // =====================================================================
    // DEMO 1: Observing Thread States
    // =====================================================================
    static void demoThreadStates() throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(100); // Will be in TIMED_WAITING
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "StateThread");

        // State 1: NEW — created but not started
        System.out.println("After creation:  " + t.getState());  // NEW

        t.start();
        // State 2: RUNNABLE — started, OS will schedule it
        System.out.println("After start():   " + t.getState());  // RUNNABLE

        Thread.sleep(50); // Give it time to hit sleep()
        // State 3: TIMED_WAITING — inside Thread.sleep()
        System.out.println("During sleep():  " + t.getState());  // TIMED_WAITING

        t.join(); // Wait for it to finish
        // State 4: TERMINATED — run() completed
        System.out.println("After finished:  " + t.getState());  // TERMINATED
    }

    // =====================================================================
    // DEMO 2: sleep() — Pause the CURRENT thread
    // =====================================================================
    // sleep(ms) pauses the thread that calls it for N milliseconds.
    // It does NOT release any locks (unlike wait()).
    // It throws InterruptedException — must handle or declare it.
    //
    // Common use: simulating delays, polling intervals, rate limiting
    static void demoSleep() throws InterruptedException {
        System.out.println("Start: " + System.currentTimeMillis());

        Thread t = new Thread(() -> {
            try {
                System.out.println("  Thread sleeping for 1 second...");
                Thread.sleep(1000); // Pause THIS thread for 1 second
                // Thread goes to TIMED_WAITING state during sleep
                // After 1 second, it becomes RUNNABLE again
                System.out.println("  Thread woke up!");
            } catch (InterruptedException e) {
                // If someone calls t.interrupt() while sleeping,
                // sleep() throws InterruptedException immediately
                System.out.println("  Thread was interrupted during sleep!");
                Thread.currentThread().interrupt(); // Preserve interrupt flag
            }
        });
        t.start();
        t.join(); // Wait for demo to complete

        System.out.println("End: " + System.currentTimeMillis());
        // You'll see ~1000ms difference between Start and End
    }

    // =====================================================================
    // DEMO 3: join() — Wait for another thread to finish
    // =====================================================================
    // t.join() tells the CALLING thread: "stop and wait until t finishes"
    // t.join(timeout) waits at most N ms, then continues regardless
    //
    // Without join: main thread might print result before worker finishes
    // With join: guaranteed order — worker finishes FIRST, then main continues
    static void demoJoin() throws InterruptedException {
        // Simulating: fetch data from 2 APIs in parallel, then combine results
        StringBuilder result1 = new StringBuilder();
        StringBuilder result2 = new StringBuilder();

        Thread api1 = new Thread(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            result1.append("User data");
            System.out.println("  API 1 done");
        });

        Thread api2 = new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            result2.append("Order data");
            System.out.println("  API 2 done");
        });

        api1.start(); // Both start at the same time
        api2.start(); // Running in parallel

        // Without these joins, result1 and result2 might be EMPTY
        // because main thread would read them before API threads finish
        api1.join(); // Wait for API 1
        api2.join(); // Wait for API 2

        // Now guaranteed both results are ready
        System.out.println("  Combined: " + result1 + " + " + result2);

        // join(timeout) example:
        Thread slowThread = new Thread(() -> {
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        slowThread.start();
        slowThread.join(100); // Wait at most 100ms, then give up
        System.out.println("  Slow thread still alive? " + slowThread.isAlive()); // true — we didn't wait for it
        slowThread.interrupt(); // Clean up
    }

    // =====================================================================
    // DEMO 4: interrupt() — Politely ask a thread to stop
    // =====================================================================
    // Java does NOT have a way to forcefully kill a thread (Thread.stop() is deprecated).
    // Instead, you call t.interrupt() which:
    //   1. If thread is sleeping/waiting → throws InterruptedException
    //   2. If thread is running → sets a flag (Thread.interrupted() returns true)
    //
    // The thread must CHECK for interruption and stop voluntarily.
    // Think of it as a "please stop" request, not a kill command.
    static void demoInterrupt() throws InterruptedException {
        Thread worker = new Thread(() -> {
            int count = 0;
            // Check interrupt flag in the loop condition
            // Thread.currentThread().isInterrupted() returns true if interrupted
            while (!Thread.currentThread().isInterrupted()) {
                count++;
                if (count % 1000000 == 0) {
                    System.out.println("  Working... count = " + count);
                }
            }
            // Thread reaches here because it CHOSE to stop when interrupted
            System.out.println("  Thread stopped voluntarily after interrupt. Final count: " + count);
        }, "WorkerThread");

        worker.start();
        Thread.sleep(50); // Let it run for 50ms

        System.out.println("  Main: calling interrupt()...");
        worker.interrupt(); // Set the interrupt flag → loop condition becomes false

        worker.join(); // Wait for worker to actually stop
    }

    // =====================================================================
    // DEMO 5: Daemon Threads — Background threads that die with main
    // =====================================================================
    // Daemon thread = background thread that does NOT prevent JVM from exiting.
    //
    // | Thread Type              | Keeps JVM alive? | Example                        |
    // |--------------------------|------------------|--------------------------------|
    // | User thread (non-daemon) | YES              | main thread, threads you create|
    // | Daemon thread            | NO               | Garbage collector, custom ones |
    //
    // RULE: JVM exits when ALL user (non-daemon) threads finish.
    //       Daemon threads are killed automatically at that point.
    //       It's not just about main — JVM waits for ALL user threads.
    //       main is just one of the user threads.
    //
    //   Example:
    //       Thread t1 = new Thread(() -> sleep(10000));  // user thread
    //       t1.start();
    //       // main finishes here, but JVM stays alive because t1 is still running
    //       // Daemons also stay alive until t1 finishes
    //       // t1 finishes → no user threads left → JVM shuts down → daemons killed
    //
    // How daemon threads die:
    //   main finishes → JVM checks → no more user threads → JVM shuts down
    //   → all daemons killed ABRUPTLY (no cleanup, no finally blocks guaranteed)
    //   Unlike interrupt(), daemon death is NOT graceful — JVM pulls the plug.
    //
    // main thread is NOT a daemon — it's a user thread.
    // If main were daemon, your program would exit immediately after starting
    // because there would be no user thread keeping the JVM alive.
    //
    // Thread type is INHERITED from parent:
    //   Thread created from main (user thread) → also user thread by default
    //   Unless you call setDaemon(true) before start()
    //
    // Use for: background tasks like garbage collection, monitoring, logging
    // DON'T use for: important work like saving data (might get killed mid-write)
    //
    // Must call setDaemon(true) BEFORE start()
    static void demoDaemon() {
        Thread daemon = new Thread(() -> {
            int i = 0;
            while (true) {
                System.out.println("  Daemon running... " + i++);
                try { Thread.sleep(100); } catch (InterruptedException e) { break; }
            }
            // This might NEVER print — JVM can kill daemon threads at any time
            System.out.println("  Daemon finished (you might never see this)");
        });

        // setDaemon must be called BEFORE start()
        // If you call it after start(), you get IllegalThreadStateException
        daemon.setDaemon(true);
        daemon.start();

        // Main thread will sleep briefly then exit
        // When main (non-daemon) exits, the daemon thread is killed
        System.out.println("  Main thread: I'll exit soon, daemon will die with me");
    }

    // =====================================================================
    // DEMO 6: Retry Pattern — Retry API call N times, then fallback
    // =====================================================================
    // Real-world scenario: Call an external API, but it might be slow or fail.
    // Strategy: Try up to 3 times, wait max 2 sec per attempt.
    //   - If any attempt succeeds → use the result
    //   - If all 3 fail → give up and use a fallback response
    //
    // Combines: Thread creation, join(timeout), interrupt(), isAlive()
    static void demoRetryPattern() throws InterruptedException {
        int maxRetries = 3;
        boolean success = false;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            StringBuilder result = new StringBuilder();

            final int currentAttempt = attempt;
            Thread apiCall = new Thread(() -> {
                try {
                    // Simulate a slow/flaky API
                    // Attempt 1 & 2: takes 5 seconds (will timeout at 2 sec)
                    // Attempt 3: takes 500ms (will succeed)
                    int delay = (currentAttempt < 3) ? 5000 : 500;
                    Thread.sleep(delay);
                    result.append("API Response: User data");
                } catch (InterruptedException e) {
                    // Thread was interrupted (we timed out and called interrupt)
                    Thread.currentThread().interrupt();
                }
            });

            apiCall.start();
            apiCall.join(2000);  // Wait at most 2 seconds for this attempt

            if (!apiCall.isAlive() && result.length() > 0) {
                // Thread finished AND produced a result → success!
                System.out.println("  Attempt " + attempt + ": SUCCESS -> " + result);
                success = true;
                break; // No need to retry
            } else {
                // Either still running (timeout) or no result (failed)
                if (apiCall.isAlive()) {
                    apiCall.interrupt(); // Clean up the timed-out thread
                }
                System.out.println("  Attempt " + attempt + ": FAILED (timeout), retrying...");
            }
        }

        if (!success) {
            System.out.println("  All " + maxRetries + " attempts failed. Using fallback response.");
        }

        /*
         * Expected output:
         *   Attempt 1: FAILED (timeout), retrying...    ← 5sec API, we waited only 2sec
         *   Attempt 2: FAILED (timeout), retrying...    ← 5sec API, we waited only 2sec
         *   Attempt 3: SUCCESS -> API Response: User data  ← 500ms API, finished in time
         *
         * Flow:
         *   Attempt 1: start thread → join(2000) → 2 sec passes → still alive → interrupt → retry
         *   Attempt 2: start thread → join(2000) → 2 sec passes → still alive → interrupt → retry
         *   Attempt 3: start thread → join(2000) → 500ms later thread finishes → success!
         */
    }
}

/*
 * =====================================================================
 * SUMMARY
 * =====================================================================
 *
 * | Method      | What it does                              | Called on    |
 * |-------------|-------------------------------------------|-------------|
 * | sleep(ms)   | Pause CURRENT thread for N ms             | Thread class |
 * | join()      | Wait for TARGET thread to finish          | Thread object|
 * | join(ms)    | Wait at most N ms for target to finish    | Thread object|
 * | interrupt() | Set interrupt flag / throw exception      | Thread object|
 * | isAlive()   | Check if thread is still running          | Thread object|
 * | setDaemon() | Mark as background thread (before start)  | Thread object|
 * | getState()  | Get current state (NEW, RUNNABLE, etc.)   | Thread object|
 *
 * KEY RULES:
 * 1. sleep() does NOT release locks — wait() does (Lesson 5)
 * 2. Always handle InterruptedException — don't swallow it
 * 3. Use interrupt() to stop threads, NEVER Thread.stop()
 * 4. join() is essential for coordinating thread completion
 * 5. Daemon threads die when all non-daemon threads finish
 *
 * NEXT LESSON: L03_SharedStateAndRaceConditions.java
 *   — What happens when two threads modify the same variable? (The Problem)
 * =====================================================================
 */

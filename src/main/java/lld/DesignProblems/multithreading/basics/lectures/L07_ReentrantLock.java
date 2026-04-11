package lld.DesignProblems.multithreading.basics.lectures;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.TimeUnit;

/*
 * =====================================================================
 * LESSON 7: REENTRANT LOCK — Advanced Locking
 * =====================================================================
 *
 * synchronized works, but has limitations:
 *   1. Can't TRY to acquire lock without blocking (no tryLock)
 *   2. Can't set a TIMEOUT for acquiring the lock
 *   3. Can't INTERRUPT a thread waiting for a lock
 *   4. No FAIRNESS control (no guaranteed FIFO order)
 *   5. Only ONE wait/notify queue per monitor
 *   6. Lock is always released at end of block (can't hold across methods)
 *
 * ReentrantLock solves ALL of these. It's the "upgraded" synchronized.
 *
 * "Reentrant" means: same thread can acquire the same lock multiple times
 *   without deadlocking (same as synchronized's reentrant behavior).
 *
 * CRITICAL RULE: Always use try/finally to ensure lock is released!
 *   lock.lock();
 *   try { ... }
 *   finally { lock.unlock(); }  // ALWAYS release, even on exception
 *
 *   Unlike synchronized, the lock is NOT auto-released on exception.
 *   Forgetting unlock() = permanent deadlock for other threads!
 * =====================================================================
 */
public class L07_ReentrantLock {

    // =====================================================================
    // DEMO 1: Basic ReentrantLock — synchronized replacement
    // =====================================================================
    private int counter = 0;
    private final ReentrantLock lock = new ReentrantLock();

    void increment() {
        lock.lock();    // Acquire the lock (blocks if another thread holds it)
        try {
            counter++;  // Critical section — only one thread at a time
        } finally {
            lock.unlock();  // ALWAYS release in finally!
        }
    }

    static void demoBasicLock() throws InterruptedException {
        L07_ReentrantLock demo = new L07_ReentrantLock();

        Thread t1 = new Thread(() -> { for (int i = 0; i < 100000; i++) demo.increment(); });
        Thread t2 = new Thread(() -> { for (int i = 0; i < 100000; i++) demo.increment(); });
        t1.start(); t2.start(); t1.join(); t2.join();

        System.out.println("  Counter: " + demo.counter + " (expected 200000)");
    }

    // =====================================================================
    // DEMO 2: tryLock() — Non-blocking lock attempt
    // =====================================================================
    // "Try to acquire the lock. If you can't, do something else instead of waiting."
    //
    // Use case: Resource is busy, don't want to wait — try an alternative.
    // Example: Primary database locked? Try the replica instead.

    private final ReentrantLock resourceLock = new ReentrantLock();

    void accessResource(String threadName) {
        // tryLock() returns immediately: true if acquired, false if not
        if (resourceLock.tryLock()) {
            try {
                System.out.println("    " + threadName + ": Got the lock! Working...");
                Thread.sleep(100); // Simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                resourceLock.unlock();
                System.out.println("    " + threadName + ": Released lock.");
            }
        } else {
            // Lock is held by another thread — don't wait!
            System.out.println("    " + threadName + ": Lock busy, doing alternative work.");
        }
    }

    static void demoTryLock() throws InterruptedException {
        L07_ReentrantLock demo = new L07_ReentrantLock();

        Thread t1 = new Thread(() -> demo.accessResource("Thread-1"));
        Thread t2 = new Thread(() -> demo.accessResource("Thread-2"));

        t1.start();
        Thread.sleep(10); // Give t1 time to acquire lock
        t2.start();       // t2 will find lock busy → alternative work

        t1.join(); t2.join();
    }

    // =====================================================================
    // DEMO 3: tryLock with TIMEOUT
    // =====================================================================
    // "Try to acquire the lock, but give up after N milliseconds."
    //
    // synchronized has NO timeout — you wait forever or not at all.
    // ReentrantLock gives you fine control over how long to wait.

    void accessWithTimeout(String threadName) {
        try {
            // Wait up to 200ms for the lock
            if (resourceLock.tryLock(200, TimeUnit.MILLISECONDS)) {
                try {
                    System.out.println("    " + threadName + ": Got lock after waiting.");
                    Thread.sleep(100);
                } finally {
                    resourceLock.unlock();
                }
            } else {
                System.out.println("    " + threadName + ": Timeout! Couldn't get lock in 200ms.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("    " + threadName + ": Interrupted while waiting for lock.");
        }
    }

    static void demoTryLockTimeout() throws InterruptedException {
        L07_ReentrantLock demo = new L07_ReentrantLock();

        // t1 holds lock for 500ms
        Thread t1 = new Thread(() -> {
            demo.resourceLock.lock();
            try {
                System.out.println("    Thread-1: Holding lock for 500ms...");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                demo.resourceLock.unlock();
            }
        });

        // t2 tries to get lock with 200ms timeout — will FAIL (t1 holds for 500ms)
        Thread t2 = new Thread(() -> demo.accessWithTimeout("Thread-2"));

        t1.start();
        Thread.sleep(10);
        t2.start();

        t1.join(); t2.join();
    }

    // =====================================================================
    // DEMO 4: Fair Lock — FIFO ordering
    // =====================================================================
    // By default, ReentrantLock is UNFAIR — when the lock is released,
    // any waiting thread can grab it (even one that just arrived).
    //
    // Fair lock: threads acquire the lock in the ORDER they requested it.
    // new ReentrantLock(true) → fair lock
    // new ReentrantLock() or new ReentrantLock(false) → unfair lock
    //
    // Trade-off: Fair lock prevents starvation but is SLOWER (overhead of queue).

    static void demoFairLock() throws InterruptedException {
        ReentrantLock fairLock = new ReentrantLock(true); // FAIR
        // ReentrantLock unfairLock = new ReentrantLock(false); // UNFAIR (default)

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                fairLock.lock();
                try {
                    System.out.println("    Thread-" + id + " acquired fair lock");
                } finally {
                    fairLock.unlock();
                }
            });
        }

        // Start all threads — with fair lock, they should execute roughly in start order
        for (Thread t : threads) {
            t.start();
            Thread.sleep(10); // Small delay so they queue up in order
        }
        for (Thread t : threads) t.join();

        System.out.println("    (With fair lock, order is roughly FIFO)");
    }

    // =====================================================================
    // DEMO 5: Lock Information — Debugging tools
    // =====================================================================
    static void demoLockInfo() throws InterruptedException {
        ReentrantLock infoLock = new ReentrantLock();

        System.out.println("  isLocked():          " + infoLock.isLocked());           // false
        System.out.println("  getHoldCount():      " + infoLock.getHoldCount());       // 0

        infoLock.lock();
        System.out.println("  After lock():");
        System.out.println("    isLocked():        " + infoLock.isLocked());           // true
        System.out.println("    isHeldByCurrentThread(): " + infoLock.isHeldByCurrentThread()); // true
        System.out.println("    getHoldCount():    " + infoLock.getHoldCount());       // 1

        // Reentrant: lock again (same thread)
        infoLock.lock();
        System.out.println("  After second lock():");
        System.out.println("    getHoldCount():    " + infoLock.getHoldCount());       // 2 (locked twice!)

        infoLock.unlock(); // Must unlock same number of times as locked!
        infoLock.unlock();
        System.out.println("  After two unlock()s:");
        System.out.println("    isLocked():        " + infoLock.isLocked());           // false
    }

    // =====================================================================
    // DEMO 6: synchronized vs ReentrantLock — Side by side comparison
    // =====================================================================
    /*
     * | Feature                    | synchronized     | ReentrantLock          |
     * |----------------------------|------------------|-----------------------|
     * | Lock/unlock                | Automatic        | Manual (try/finally)  |
     * | tryLock (non-blocking)     | NO               | YES                   |
     * | Timeout                    | NO               | YES (tryLock(time))   |
     * | Interruptible waiting      | NO               | YES (lockInterruptibly)|
     * | Fair ordering              | NO               | YES (fair=true)       |
     * | Multiple conditions       | NO (one monitor)  | YES (newCondition())  |
     * | Performance                | Similar          | Similar               |
     * | Risk of forgetting unlock  | Impossible       | YES — always finally! |
     * | Reentrant                  | YES              | YES                   |
     *
     * RULE OF THUMB:
     *   Use synchronized for simple cases (easier, safer — auto-release).
     *   Use ReentrantLock when you need tryLock, timeout, fairness, or Conditions.
     */

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== DEMO 1: Basic ReentrantLock ===\n");
        demoBasicLock();

        System.out.println("\n=== DEMO 2: tryLock (Non-blocking) ===\n");
        demoTryLock();

        System.out.println("\n=== DEMO 3: tryLock with Timeout ===\n");
        demoTryLockTimeout();

        System.out.println("\n=== DEMO 4: Fair Lock (FIFO) ===\n");
        demoFairLock();

        System.out.println("\n=== DEMO 5: Lock Information ===\n");
        demoLockInfo();

        System.out.println("\nDone!");
    }
}

/*
 * =====================================================================
 * SUMMARY
 * =====================================================================
 *
 * ReentrantLock = synchronized on steroids.
 * Use it when you need:
 *   - tryLock()            → "Can I get the lock? If not, do something else"
 *   - tryLock(timeout)     → "Wait up to N ms for the lock"
 *   - lockInterruptibly()  → "Wait for lock, but can be interrupted"
 *   - new ReentrantLock(true) → Fair FIFO ordering
 *   - lock.newCondition()  → Multiple wait/signal queues (Lesson 9)
 *
 * ALWAYS:
 *   lock.lock();
 *   try { ... }
 *   finally { lock.unlock(); }
 *
 * NEVER forget unlock() — it won't auto-release like synchronized!
 *
 * ALL 4 RACE CONDITION SOLUTIONS NOW COVERED:
 *   L04: synchronized     — simple, auto-release
 *   L05: volatile          — visibility only, no locking
 *   L06: AtomicInteger/CAS — lock-free, single variable ops
 *   L07: ReentrantLock     — advanced locking, full control
 *
 * NEXT LESSON: L08_WaitNotify.java — Thread communication
 * =====================================================================
 */

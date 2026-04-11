package lld.DesignProblems.multithreading.basics.lectures;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/*
 * =====================================================================
 * LESSON 6: ATOMIC CLASSES & CAS (Compare-And-Swap)
 * =====================================================================
 *
 * Problem recap:
 *   volatile solves visibility but NOT atomicity (counter++ still broken).
 *   synchronized solves both but is SLOW (blocking, context switching).
 *
 * Atomic classes solve both WITHOUT locking — using a hardware trick called CAS.
 *
 * CAS (Compare-And-Swap):
 *   A single CPU instruction that does 3 things atomically:
 *   1. READ current value
 *   2. COMPARE with expected value
 *   3. If match → SWAP with new value. If not → RETRY.
 *
 *   It's like saying: "I think the value is 100. If it's still 100,
 *   change it to 101. If someone changed it, I'll re-read and try again."
 *
 * Why is CAS faster than synchronized?
 *   synchronized: thread BLOCKS (goes to sleep, context switch, OS overhead)
 *   CAS: thread SPINS (retries in a loop — no blocking, no OS involvement)
 *   For short operations like counter++, spinning is much faster.
 *
 * Available Atomic classes:
 *   AtomicInteger, AtomicLong, AtomicBoolean, AtomicReference<T>
 *   AtomicIntegerArray, AtomicLongArray, AtomicReferenceArray
 * =====================================================================
 */
public class L06_AtomicClasses {

    // =====================================================================
    // DEMO 1: AtomicInteger — Fixing counter++ without locks
    // =====================================================================

    private static int unsafeCounter = 0;
    private static final AtomicInteger atomicCounter = new AtomicInteger(0);

    static void demoAtomicInteger() throws InterruptedException {
        unsafeCounter = 0;
        atomicCounter.set(0);

        // Unsafe version
        Thread t1 = new Thread(() -> { for (int i = 0; i < 100000; i++) unsafeCounter++; });
        Thread t2 = new Thread(() -> { for (int i = 0; i < 100000; i++) unsafeCounter++; });
        t1.start(); t2.start(); t1.join(); t2.join();

        // Atomic version
        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                atomicCounter.incrementAndGet();
                // incrementAndGet() internally does CAS:
                //   1. Read current value (say 100)
                //   2. Try to set it to 101
                //   3. If another thread changed it → retry with new value
                //   This loop continues until the CAS succeeds.
                //   All of this happens WITHOUT any lock!
            }
        });
        Thread t4 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                atomicCounter.incrementAndGet();
            }
        });
        t3.start(); t4.start(); t3.join(); t4.join();

        System.out.println("  Unsafe counter:  " + unsafeCounter + " (expected 200000)");
        System.out.println("  Atomic counter:  " + atomicCounter.get() + " (expected 200000)");
    }

    // =====================================================================
    // DEMO 2: Common AtomicInteger Operations
    // =====================================================================
    static void demoAtomicOperations() {
        AtomicInteger ai = new AtomicInteger(10);

        // get() — read current value
        System.out.println("  get():              " + ai.get());              // 10

        // set() — write value (same as volatile write)
        ai.set(20);
        System.out.println("  set(20):            " + ai.get());              // 20

        // incrementAndGet() — ++counter (returns new value)
        System.out.println("  incrementAndGet():  " + ai.incrementAndGet());  // 21

        // getAndIncrement() — counter++ (returns old value)
        System.out.println("  getAndIncrement():  " + ai.getAndIncrement());  // 21 (returns old)
        System.out.println("  after:              " + ai.get());              // 22

        // addAndGet() — add N and return new value
        System.out.println("  addAndGet(8):       " + ai.addAndGet(8));       // 30

        // compareAndSet() — the raw CAS operation
        // "If current value is 30, set it to 50. Return true if succeeded."
        boolean success = ai.compareAndSet(30, 50);
        System.out.println("  compareAndSet(30,50): " + success + ", value: " + ai.get()); // true, 50

        // compareAndSet fails if current value doesn't match expected
        boolean failed = ai.compareAndSet(30, 99); // current is 50, not 30!
        System.out.println("  compareAndSet(30,99): " + failed + ", value: " + ai.get());  // false, still 50

        // getAndUpdate() / updateAndGet() — apply a function atomically
        // Example: multiply by 2 atomically
        int result = ai.updateAndGet(x -> x * 2);
        System.out.println("  updateAndGet(x*2):  " + result);               // 100
    }

    // =====================================================================
    // DEMO 3: AtomicBoolean — Thread-safe flag
    // =====================================================================
    // Better than volatile boolean when you need compareAndSet
    // (e.g., ensure only ONE thread starts initialization)

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    static void demoAtomicBoolean() throws InterruptedException {
        // 5 threads try to initialize, but only ONE should succeed
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                // compareAndSet: "If false, set to true and return true. Otherwise return false."
                // Only ONE thread will get true — the rest get false.
                // This is atomic — no race condition possible!
                if (initialized.compareAndSet(false, true)) {
                    System.out.println("  Thread " + id + ": I initialized the system!");
                } else {
                    System.out.println("  Thread " + id + ": Already initialized, skipping.");
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();
    }

    // =====================================================================
    // DEMO 4: AtomicReference — Thread-safe object reference
    // =====================================================================
    // When you need to atomically swap an entire object, not just a number.

    static void demoAtomicReference() {
        // Thread-safe holder for any object
        AtomicReference<String> currentLeader = new AtomicReference<>("Alice");

        System.out.println("  Current leader: " + currentLeader.get());

        // Atomic swap: "If leader is Alice, change to Bob"
        boolean swapped = currentLeader.compareAndSet("Alice", "Bob");
        System.out.println("  Swap Alice->Bob: " + swapped + ", leader: " + currentLeader.get());

        // This will fail: leader is Bob now, not Alice
        boolean failed = currentLeader.compareAndSet("Alice", "Charlie");
        System.out.println("  Swap Alice->Charlie: " + failed + ", leader: " + currentLeader.get());

        // Force update regardless of current value
        currentLeader.set("Charlie");
        System.out.println("  Force set Charlie: " + currentLeader.get());
    }

    // =====================================================================
    // DEMO 5: CAS under the hood — what actually happens
    // =====================================================================
    static void demoCASExplained() {
        AtomicInteger counter = new AtomicInteger(0);

        // This is what incrementAndGet() does internally (pseudocode):
        //
        // int incrementAndGet() {
        //     while (true) {                      // spin loop (retry)
        //         int current = this.value;        // 1. READ current value
        //         int next = current + 1;          // 2. Calculate new value
        //         if (compareAndSet(current, next)) // 3. CAS: try to update
        //             return next;                  //    Success → return
        //         // CAS failed (another thread changed it) → retry the whole thing
        //     }
        // }
        //
        // Why this works:
        //   If two threads read "100" simultaneously:
        //   Thread 1: CAS(100, 101) → SUCCESS (value is now 101)
        //   Thread 2: CAS(100, 101) → FAILS! (value is 101, not 100 anymore)
        //   Thread 2: retries → reads 101 → CAS(101, 102) → SUCCESS
        //
        //   No lost updates! No locks! Just retries.

        // Manual CAS loop example:
        int expected, newValue;
        do {
            expected = counter.get();       // Read
            newValue = expected + 5;        // Calculate
        } while (!counter.compareAndSet(expected, newValue)); // CAS — retry if failed

        System.out.println("  Manual CAS result: " + counter.get()); // 5
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== DEMO 1: AtomicInteger vs Unsafe Counter ===\n");
        demoAtomicInteger();

        System.out.println("\n=== DEMO 2: AtomicInteger Operations ===\n");
        demoAtomicOperations();

        System.out.println("\n=== DEMO 3: AtomicBoolean (One-time Init) ===\n");
        demoAtomicBoolean();

        System.out.println("\n=== DEMO 4: AtomicReference ===\n");
        demoAtomicReference();

        System.out.println("\n=== DEMO 5: CAS Under the Hood ===\n");
        demoCASExplained();

        System.out.println("\nDone!");
    }
}

/*
 * =====================================================================
 * SUMMARY: ALL 4 SOLUTIONS TO RACE CONDITIONS
 * =====================================================================
 *
 * | Solution       | Visibility | Atomicity | Blocking? | Best for                    |
 * |----------------|------------|-----------|-----------|------------------------------|
 * | volatile       | YES        | NO        | No        | Flags, single read/write     |
 * | synchronized   | YES        | YES       | YES       | Compound ops, critical sections|
 * | AtomicInteger  | YES        | YES       | No (CAS)  | Single variable operations   |
 * | ReentrantLock  | YES        | YES       | YES       | Advanced locking (Lesson 7)  |
 *
 * WHEN TO USE ATOMIC vs SYNCHRONIZED:
 *   Atomic:       counter++, compareAndSet, simple update on ONE variable
 *   Synchronized: multiple variables, check-then-act, complex logic
 *
 *   Example — Atomic is ENOUGH:
 *     atomicCounter.incrementAndGet();  // one variable, one operation
 *
 *   Example — Need synchronized:
 *     if (balance >= amount) balance -= amount;  // read + check + write = compound
 *     // Can't do this atomically with just AtomicInteger
 *
 * WHEN CAS PERFORMS POORLY (ABA Problem):
 *   High contention (many threads, lots of retries) → CAS spins waste CPU
 *   In that case, synchronized/ReentrantLock (blocking) is actually better.
 *
 * NEXT LESSON: L07_ReentrantLock.java — Advanced locking with tryLock, fairness, Conditions
 * =====================================================================
 */

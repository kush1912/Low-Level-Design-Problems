package lld.DesignProblems.multithreading.basics.lectures;

/*
 * =====================================================================
 * LESSON 3: SHARED STATE & RACE CONDITIONS (The Problem)
 * =====================================================================
 *
 * What is Shared State?
 *   When multiple threads read/write the SAME variable or object.
 *   This is where multithreading bugs come from.
 *
 * What is a Race Condition?
 *   A bug where the result depends on the TIMING of thread execution.
 *   Two threads "race" to modify shared data, and the outcome is
 *   unpredictable — sometimes correct, sometimes wrong.
 *
 * Real-world analogy:
 *   Bank account with $1000. Two ATMs try to withdraw $700 at the same time.
 *   Both read $1000, both think it's enough, both withdraw → account goes -$400!
 *   That's a race condition.
 *
 * This lesson shows THE PROBLEM. Lesson 4 shows THE SOLUTION (synchronized).
 * =====================================================================
 */
public class L03_RaceCondition {

    // =====================================================================
    // DEMO 1: The Classic Counter Problem
    // =====================================================================
    // Two threads increment the same counter 100,000 times each.
    // Expected result: 200,000
    // Actual result: Something LESS than 200,000 (different every run!)

    private static int counter = 0; // SHARED mutable state — the root of all evil
    // Why static (class-level) and not local?
    //
    // Local variables live on the STACK (private to each thread).
    // Java won't even let you modify a local variable from a lambda:
    //     int counter = 0;
    //     Thread t = new Thread(() -> counter++);  // COMPILE ERROR!
    //     Lambdas require local variables to be "effectively final" (never modified).
    //
    // To create a race condition, the variable must be on the HEAP (shared memory).
    // Two ways to put it on the heap:
    //   1. static field  -> shared across ALL threads (used here)
    //   2. instance field -> shared if threads access the SAME object
    //
    // | Variable type  | Stored in          | Shared?          | Race condition? |
    // |----------------|--------------------|------------------|-----------------|
    // | Local variable | Stack (per thread) | No               | No              |
    // | Instance field | Heap (per object)  | Yes (same object)| Yes             |
    // | Static field   | Heap (per class)   | Yes (always)     | Yes             |

    static void demoCounterProblem() throws InterruptedException {
        counter = 0;

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter++; // NOT atomic! (explained below)
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter++; // Same shared variable
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("  Expected: 200000");
        System.out.println("  Actual:   " + counter);
        System.out.println("  Lost:     " + (200000 - counter) + " increments!");

        /*
         * WHY IS THE RESULT WRONG?
         *
         * counter++ looks like ONE operation but it's actually THREE:
         *   1. READ:  read current value of counter from memory
         *   2. ADD:   add 1 to it
         *   3. WRITE: write new value back to memory
         *
         * When two threads do this simultaneously:
         *
         *   Thread 1                Thread 2
         *   --------                --------
         *   READ counter = 100
         *                           READ counter = 100  (SAME value!)
         *   ADD: 100 + 1 = 101
         *                           ADD: 100 + 1 = 101  (SAME result!)
         *   WRITE counter = 101
         *                           WRITE counter = 101  (OVERWRITES!)
         *
         *   Result: counter = 101 (should be 102)
         *   We LOST one increment! This is called a "lost update".
         *
         * This happens randomly depending on thread scheduling,
         * which is why the result is different every time you run it.
         */
    }

    // =====================================================================
    // DEMO 2: The Bank Account Problem (Real-world race condition)
    // =====================================================================
    // Two ATMs withdrawing from the same account simultaneously.

    static int balance = 1000;

    static void demoBankProblem() throws InterruptedException {
        balance = 1000;

        // ATM 1 tries to withdraw 700
        Thread atm1 = new Thread(() -> {
            // Step 1: Check balance
            if (balance >= 700) {
                // Simulate some processing delay (makes race condition more visible)
                try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                // Step 2: Withdraw
                balance -= 700;
                System.out.println("  ATM 1: Withdrew 700. Balance: " + balance);
            } else {
                System.out.println("  ATM 1: Insufficient funds!");
            }
        });

        // ATM 2 also tries to withdraw 700
        Thread atm2 = new Thread(() -> {
            // Step 1: Check balance (reads 1000 — same as ATM 1!)
            if (balance >= 700) {
                try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                // Step 2: Withdraw (also thinks 1000 is available!)
                balance -= 700;
                System.out.println("  ATM 2: Withdrew 700. Balance: " + balance);
            } else {
                System.out.println("  ATM 2: Insufficient funds!");
            }
        });

        atm1.start();
        atm2.start();
        atm1.join();
        atm2.join();

        System.out.println("  Final balance: " + balance);
        System.out.println("  (Should never be negative! But it might be: " + balance + ")");

        /*
         * WHAT HAPPENED?
         *
         *   ATM 1                      ATM 2
         *   -----                      -----
         *   CHECK: balance=1000 >= 700? YES
         *                              CHECK: balance=1000 >= 700? YES
         *   (both passed the check before either withdrew!)
         *   WITHDRAW: balance = 1000 - 700 = 300
         *                              WITHDRAW: balance = 300 - 700 = -400 !!!
         *
         * The CHECK and WITHDRAW are NOT atomic — another thread can
         * sneak in between them. This is called a "check-then-act" race condition.
         *
         * This is the SAME bug that causes:
         *   - Double booking in ticket systems
         *   - Overselling in e-commerce
         *   - Double spending in payment systems
         */
    }

    // =====================================================================
    // DEMO 3: Visibility Problem — Thread can't see other thread's changes
    // =====================================================================
    // Even without interleaving, threads might not SEE each other's writes!

    private static boolean running = true; // Shared flag

    static void demoVisibilityProblem() throws InterruptedException {
        Thread worker = new Thread(() -> {
            int count = 0;
            // This loop might NEVER end, even after main sets running = false!
            // Why? Each thread can cache variables in its CPU register/cache.
            // Without volatile or synchronization, the worker thread might
            // never see the updated value from the main thread.
            while (running) {
                count++;
            }
            System.out.println("  Worker stopped after count: " + count);
        });

        worker.start();
        Thread.sleep(100); // Let worker run for 100ms

        System.out.println("  Main: setting running = false...");
        running = false;    // Main thread updates the flag

        worker.join(2000);  // Wait at most 2 seconds

        if (worker.isAlive()) {
            // Worker might STILL be running because it never saw running=false!
            System.out.println("  Worker is STILL RUNNING! (visibility problem)");
            System.out.println("  The worker thread's CPU cache has a stale copy of 'running=true'");
            System.out.println("  Fix: use 'volatile' keyword → private static volatile boolean running");
            worker.interrupt(); // Force stop for demo cleanup
        }

        /*
         * THE VISIBILITY PROBLEM:
         *
         * Each CPU core has its own cache. When a thread reads a variable,
         * it might read from its LOCAL CACHE instead of main memory.
         *
         *   Main Memory:  running = false  (updated by main thread)
         *   CPU Cache 1:  running = true   (worker thread's stale copy!)
         *
         * The worker thread never sees the update because it's reading
         * from its own cache.
         *
         * Fix options:
         *   1. volatile keyword → forces reads/writes from main memory
         *   2. synchronized    → guarantees visibility + atomicity
         *   3. Atomic classes  → AtomicBoolean, AtomicInteger, etc.
         *
         * NOTE: This demo might or might not show the problem depending
         * on JVM optimizations and hardware. In production code, you MUST
         * use volatile/synchronized — never rely on "it worked on my machine".
         */
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== DEMO 1: Counter Race Condition ===\n");
        // Run it 3 times to show different results each time
        for (int run = 1; run <= 3; run++) {
            System.out.print("  Run " + run + " -> ");
            demoCounterProblem();
            System.out.println();
        }

        System.out.println("=== DEMO 2: Bank Account Race Condition ===\n");
        demoBankProblem();

        System.out.println("\n=== DEMO 3: Visibility Problem ===\n");
        demoVisibilityProblem();

        System.out.println("\nDone!");
    }
}

/*
 * =====================================================================
 * SUMMARY — THE THREE PROBLEMS OF MULTITHREADING
 * =====================================================================
 *
 * 1. ATOMICITY (Demo 1 & 2):
 *    counter++ is NOT one operation — it's read + add + write.
 *    Another thread can interfere between these steps.
 *    Fix: synchronized, ReentrantLock, or AtomicInteger
 *
 * 2. VISIBILITY (Demo 3):
 *    Thread might not see another thread's writes (CPU cache).
 *    Fix: volatile, synchronized, or Atomic classes
 *
 * 3. ORDERING:
 *    JVM/CPU can reorder instructions for optimization.
 *    What you wrote:  a = 1; b = 2;
 *    What might run:  b = 2; a = 1;  (reordered!)
 *    Fix: volatile, synchronized (establishes happens-before)
 *
 * THESE are the fundamental problems. All synchronization tools
 * (synchronized, Lock, volatile, Atomic, etc.) exist to solve
 * one or more of these three problems.
 *
 * KEY TAKEAWAY:
 *   Shared mutable state + multiple threads = BUGS
 *   Solutions:
 *     1. Don't share (each thread has its own data)
 *     2. Don't mutate (use immutable/final objects)
 *     3. Synchronize access (locks, atomic classes)
 *
 * NEXT LESSON: L04_Synchronized.java — The solution to race conditions
 * =====================================================================
 */

package lld.concurrency.multithreading.basics.lectures;

/*
 * =====================================================================
 * LESSON 5: VOLATILE — Solving the Visibility Problem
 * =====================================================================
 *
 * In Lesson 3 (Demo 3) we saw the VISIBILITY problem:
 *   One thread updates a variable, but another thread can't see the change
 *   because it's reading from its CPU cache (stale copy).
 *
 * volatile keyword solves this:
 *   - Every READ goes directly to MAIN MEMORY (not CPU cache)
 *   - Every WRITE goes directly to MAIN MEMORY (not CPU cache)
 *   - Prevents instruction reordering around volatile access
 *
 * Analogy:
 *   Without volatile: each person has a notebook (CPU cache) with a copy.
 *     Person A updates the whiteboard, butNe Person B keeps reading their notebook.
 *   With volatile: NO notebooks allowed. Everyone reads/writes the whiteboard directly.
 *
 * IMPORTANT: volatile solves VISIBILITY, but NOT ATOMICITY.
 *   volatile counter++  → still broken! (read + add + write = 3 steps)
 *   volatile flag = true → works! (single write = 1 step)
 *
 * Rule of thumb:
 *   Use volatile when: ONE thread WRITES, other threads only READ.
 *   Don't use when: multiple threads WRITE (use synchronized or Atomic instead).
 * =====================================================================
 */
public class L05_Volatile {

    // =====================================================================
    // DEMO 1: Fixing the Visibility Problem from Lesson 3
    // =====================================================================
    // Without volatile: worker thread might never see running = false
    // With volatile: worker thread sees the change immediately

    // --- BROKEN: without volatile ---
    private static boolean runningBroken = true;

    // --- FIXED: with volatile ---
    // volatile forces every read/write to go through main memory
    // No CPU cache staleness — all threads see the latest value immediately
    private static volatile boolean runningFixed = true;

    static void demoVisibilityFix() throws InterruptedException {
        // First: demonstrate the BROKEN version
        runningBroken = true;
        Thread brokenWorker = new Thread(() -> {
            int count = 0;
            while (runningBroken) { // Might read stale value from CPU cache forever!
                count++;
            }
            System.out.println("  Broken worker stopped at count: " + count);
        });

        brokenWorker.start();
        Thread.sleep(100);
        runningBroken = false;
        brokenWorker.join(1000); // Wait max 1 second

        if (brokenWorker.isAlive()) {
            System.out.println("  Broken worker: STILL RUNNING (visibility problem!)");
            brokenWorker.interrupt();
        }

        // Now: demonstrate the FIXED version
        runningFixed = true;
        Thread fixedWorker = new Thread(() -> {
            int count = 0;
            while (runningFixed) { // volatile: always reads from main memory
                count++;
            }
            System.out.println("  Fixed worker stopped at count: " + count);
        });

        fixedWorker.start();
        Thread.sleep(100);
        runningFixed = false; // volatile write: immediately visible to all threads
        fixedWorker.join(1000);

        if (fixedWorker.isAlive()) {
            System.out.println("  Fixed worker: STILL RUNNING (shouldn't happen!)");
            fixedWorker.interrupt();
        } else {
            System.out.println("  Fixed worker: Stopped correctly (volatile works!)");
        }
    }

    // =====================================================================
    // DEMO 2: volatile does NOT solve atomicity
    // =====================================================================
    // volatile makes reads/writes visible, but counter++ is still 3 steps:
    //   1. READ from main memory  (volatile guarantees fresh read)
    //   2. ADD 1
    //   3. WRITE to main memory   (volatile guarantees immediate write)
    //
    // But another thread can still READ between step 1 and step 3!
    // volatile doesn't lock — it only guarantees visibility.

    private static volatile int volatileCounter = 0; // volatile but STILL broken for ++

    static void demoVolatileNotAtomic() throws InterruptedException {
        volatileCounter = 0;

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                volatileCounter++; // STILL NOT ATOMIC even with volatile!
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                volatileCounter++;
            }
        });

        t1.start(); t2.start();
        t1.join(); t2.join();

        System.out.println("  volatile counter: " + volatileCounter + " (expected 200000)");
        System.out.println("  Still broken! volatile != synchronized");

        /*
         * WHY?
         *
         * Thread 1                  Thread 2
         * --------                  --------
         * READ counter = 100        
         * (volatile: fresh from main memory ✓)
         *                           READ counter = 100
         *                           (volatile: fresh from main memory ✓)
         * ADD: 101
         *                           ADD: 101
         * WRITE: counter = 101
         * (volatile: goes to main memory ✓)
         *                           WRITE: counter = 101
         *                           (volatile: goes to main memory ✓)
         *
         * Result: 101 instead of 102. Lost update!
         * volatile ensured visibility, but the 3-step operation
         * was still interleaved. Need synchronized or AtomicInteger for this.
         */
    }

    // =====================================================================
    // DEMO 3: When volatile IS the right choice
    // =====================================================================
    // Pattern: one writer, multiple readers (flags, status, config)

    private static volatile boolean initialized = false;
    private static String config = null;

    // Thread 1: initializes config, then sets flag
    // Thread 2: waits for flag, then reads config
    //
    // Without volatile on 'initialized':
    //   Thread 2 might see initialized=true but config=null!
    //   Why? CPU can REORDER: set flag before config is ready.
    //
    // With volatile on 'initialized':
    //   volatile prevents reordering. Everything BEFORE the volatile write
    //   is guaranteed to be visible when the volatile read sees the new value.
    //   This is called the "happens-before" guarantee.

    static void demoCorrectUsage() throws InterruptedException {
        initialized = false;
        config = null;

        // Writer thread: prepare config, then signal ready
        Thread writer = new Thread(() -> {
            config = "database=localhost:5432";  // Step 1: prepare data
            initialized = true;                  // Step 2: signal ready (volatile write)
            // volatile guarantees: config write HAPPENS-BEFORE initialized write
            // So any thread that reads initialized=true WILL see the config value
            System.out.println("  Writer: config set, flag = true");
        });

        // Reader thread: wait for signal, then use config
        Thread reader = new Thread(() -> {
            while (!initialized) {
                // Busy-wait until volatile flag becomes true
                // volatile read ensures we see the latest value
            }
            // If we get here, initialized=true
            // happens-before guarantees config is also visible
            System.out.println("  Reader: config = " + config);
        });

        reader.start();
        Thread.sleep(10); // Let reader start waiting
        writer.start();

        writer.join();
        reader.join(1000);

        if (reader.isAlive()) {
            System.out.println("  Reader stuck (shouldn't happen with volatile)");
            reader.interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== DEMO 1: Volatile Fixes Visibility ===\n");
        demoVisibilityFix();

        System.out.println("\n=== DEMO 2: Volatile Does NOT Fix Atomicity ===\n");
        demoVolatileNotAtomic();

        System.out.println("\n=== DEMO 3: Correct Usage (Flag Pattern) ===\n");
        demoCorrectUsage();

        System.out.println("\nDone!");
    }
}

/*
 * =====================================================================
 * SUMMARY: WHEN TO USE WHAT?
 * =====================================================================
 *
 * | Problem          | volatile | synchronized | AtomicInteger |
 * |------------------|----------|--------------|---------------|
 * | Visibility       | YES      | YES          | YES           |
 * | Atomicity        | NO       | YES          | YES           |
 * | Reordering       | YES      | YES          | YES           |
 * | Performance      | Fast     | Slower       | Fast          |
 *
 * =====================================================================
 * WHAT IS RE-ORDERING?
 * =====================================================================
 * The JVM and CPU can CHANGE the order of instructions for optimization,
 * as long as the result is the same in a SINGLE thread.
 *
 *   What you wrote:              What CPU might execute:
 *     int a = 1;  // Step 1        int b = 2;  // Step 2 first!
 *     int b = 2;  // Step 2        int a = 1;  // Step 1 second!
 *     int c = a+b;// Step 3        int c = a+b;// result still 3, CPU doesn't care
 *
 * Single thread? No problem — result is always correct.
 * Multiple threads? DANGEROUS:
 *
 *   Thread 1:                          CPU might REORDER to:
 *     config = loadConfig(); // Step 1   initialized = true;  // Step 2 FIRST!
 *     initialized = true;    // Step 2   config = loadConfig();// Step 1 SECOND
 *
 *   Thread 2:
 *     if (initialized) {
 *         use(config);   // CRASH! config is still null — reordering broke it
 *     }
 *
 * FIX: volatile prevents reordering around it:
 *   volatile boolean initialized = false;
 *   // Now Step 1 is GUARANTEED to complete before Step 2
 *   // This is the "happens-before" guarantee
 *
 * =====================================================================
 * PERFORMANCE COMPARISON
 * =====================================================================
 *
 *   FAST <-----------------------------------------------> SLOW
 *   No sync    volatile    AtomicInteger    synchronized    ReentrantLock
 *   (unsafe)   (visibility  (CAS spin)      (JVM monitor)   (AQS park/
 *              only)        (no blocking)                    unpark)
 *
 * | Solution       | Cost vs plain read | Why                              |
 * |----------------|--------------------|----------------------------------|
 * | volatile       | ~2-5x slower       | Every read/write to main memory, |
 * |                |                    | skipping CPU cache. No locking.  |
 * | AtomicInteger  | ~5-10x slower      | CAS spin loop — high contention  |
 * |                |                    | means threads keep retrying,     |
 * |                |                    | burning CPU.                     |
 * | synchronized   | ~10-20x slower     | OS context switch — thread sleeps|
 * |                |                    | gets woken up later. Expensive   |
 * |                |                    | if lock is held briefly.         |
 * | ReentrantLock  | Similar to sync    | Also blocks, uses park/unpark —  |
 * |                |                    | slightly more efficient sometimes|
 *
 * When does performance matter?
 *   Low contention (few threads):       ALL are fast, pick simplest
 *   High contention (many threads):     synchronized/Lock wins over CAS
 *                                       (blocking > spinning when wait is long)
 *   Tight loop, single variable:        volatile or Atomic wins
 *                                       (no blocking overhead)
 *
 * =====================================================================
 *
 * USE VOLATILE WHEN:
 *   - One thread writes, others only read (flags, status, config)
 *   - Single variable, single write operation (not counter++)
 *   - You need visibility guarantee without full locking overhead
 *
 * DON'T USE VOLATILE WHEN:
 *   - Multiple threads write to the same variable
 *   - Compound operations (read-modify-write like counter++)
 *   - Check-then-act patterns (if balance >= amount then withdraw)
 *   -> Use synchronized or AtomicInteger instead
 *
 * COMMON VOLATILE PATTERNS:
 *   1. Shutdown flag:        volatile boolean running = true;
 *   2. Initialization flag:  volatile boolean initialized = false;
 *   3. Status indicator:     volatile String status = "STARTING";
 *   4. Double-checked lock:  volatile Singleton instance; (Lesson 7)
 *
 * NEXT LESSON: L06_AtomicClasses.java — Lock-free thread safety with CAS
 * =====================================================================
 */

package lld.DesignProblems.multithreading.basics.lectures;

/*
 * =====================================================================
 * LESSON 4: SYNCHRONIZED — The Solution to Race Conditions
 * =====================================================================
 *
 * In Lesson 3 we saw the problem: two threads modifying shared state
 * causes data corruption (race conditions).
 *
 * The fix: synchronized — ensures only ONE thread can execute a block
 * of code at a time. Other threads must WAIT.
 *
 * How it works:
 *   Every Java object has an invisible "lock" (called intrinsic lock or monitor).
 *   synchronized acquires this lock before entering the block.
 *   If another thread already holds the lock, the current thread BLOCKS (waits).
 *
 * Analogy:
 *   synchronized = a bathroom with one key.
 *   First person takes the key, goes in, locks the door.
 *   Second person arrives, no key available — WAITS outside.
 *   First person finishes, returns key — second person can enter.
 *
 * Three ways to use synchronized:
 *   1. Synchronized method         — locks on 'this' (instance) or Class (static)
 *   2. Synchronized block          — locks on any object you choose
 *   3. Static synchronized method  — locks on the Class object
 * =====================================================================
 */
public class L04_Synchronized {

    // =====================================================================
    // DEMO 1: Fixing the Counter Problem from Lesson 3
    // =====================================================================

    // --- BROKEN version (from Lesson 3) ---
    private static int brokenCounter = 0;

    static void incrementBroken() {
        brokenCounter++; // NOT thread-safe: read + add + write can be interleaved
    }

    // --- FIXED version using synchronized method ---
    private static int fixedCounter = 0;

    // synchronized on a static method locks on the CLASS object (L04_Synchronized.class)
    // Only ONE thread can execute this method at a time.
    // Other threads trying to call this method will BLOCK until the lock is released.
    //
    // What happens internally:
    //   Thread 1 calls incrementFixed()
    //   → acquires lock on L04_Synchronized.class
    //   → executes counter++ (read + add + write — all protected)
    //   → releases lock
    //   Thread 2 was waiting → now acquires lock → executes → releases
    //
    // The three steps (read, add, write) are now ATOMIC — no thread can
    // sneak in between them because the lock prevents it.
    static synchronized void incrementFixed() {
        fixedCounter++; // Thread-safe: lock ensures only one thread at a time
    }

    static void demoSynchronizedMethod() throws InterruptedException {
        brokenCounter = 0;
        fixedCounter = 0;

        // Broken version
        Thread t1 = new Thread(() -> { for (int i = 0; i < 100000; i++) incrementBroken(); });
        Thread t2 = new Thread(() -> { for (int i = 0; i < 100000; i++) incrementBroken(); });
        t1.start(); t2.start(); t1.join(); t2.join();

        // Fixed version
        Thread t3 = new Thread(() -> { for (int i = 0; i < 100000; i++) incrementFixed(); });
        Thread t4 = new Thread(() -> { for (int i = 0; i < 100000; i++) incrementFixed(); });
        t3.start(); t4.start(); t3.join(); t4.join();

        System.out.println("  Broken counter: " + brokenCounter + " (expected 200000)");
        System.out.println("  Fixed counter:  " + fixedCounter + " (expected 200000)");
    }

    // =====================================================================
    // DEMO 2: Synchronized Block — More control over what gets locked
    // =====================================================================
    // Synchronized METHOD locks the entire method.
    // Synchronized BLOCK locks only the critical section (the part that
    // accesses shared state). The rest of the method runs without the lock.
    //
    // Why use a block instead of a method?
    //   - Only lock what needs to be locked (better performance)
    //   - Choose WHICH object to lock on (flexibility)
    //   - Multiple independent shared resources can use different locks

    private int balance = 1000;
    private final Object balanceLock = new Object(); // Lock object for balance
    // Why a separate lock object?
    //   - Using 'this' means ALL synchronized methods/blocks in this class
    //     share the SAME lock. One blocks the other even if unrelated.
    //   - A dedicated lock object lets you protect different resources independently.

    void withdraw(int amount) {
        // Non-critical code: logging, validation — runs WITHOUT lock
        System.out.println("    " + Thread.currentThread().getName() + " wants to withdraw " + amount);

        // Critical section: only this part needs the lock
        synchronized (balanceLock) {
            // synchronized(balanceLock) means:
            //   "Acquire the lock on balanceLock object before entering this block.
            //    If another thread holds it, WAIT here until they release it."
            if (balance >= amount) {
                // Simulate processing delay
                try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                balance -= amount;
                System.out.println("    " + Thread.currentThread().getName() + " withdrew " + amount + ". Balance: " + balance);
            } else {
                System.out.println("    " + Thread.currentThread().getName() + " REJECTED. Balance: " + balance);
            }
        }
        // Lock is automatically released when exiting the synchronized block
        // Even if an exception is thrown inside — the lock is ALWAYS released.
    }

    static void demoSynchronizedBlock() throws InterruptedException {
        L04_Synchronized bank = new L04_Synchronized();

        Thread atm1 = new Thread(() -> bank.withdraw(700), "ATM-1");
        Thread atm2 = new Thread(() -> bank.withdraw(700), "ATM-2");

        atm1.start();
        atm2.start();
        atm1.join();
        atm2.join();

        System.out.println("    Final balance: " + bank.balance + " (never negative!)");
    }

    // =====================================================================
    // DEMO 3: synchronized(this) vs synchronized(lockObject) vs Class lock
    // =====================================================================
    // Showing what each locks on and when to use which.

    private int count1 = 0;
    private int count2 = 0;
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    // Option A: synchronized(this) — locks on the entire object
    // Problem: count1 and count2 are independent, but this blocks BOTH
    void incrementCount1_this() {
        synchronized (this) {    // Locks on 'this' — the L04_Synchronized instance
            count1++;
        }
    }
    void incrementCount2_this() {
        synchronized (this) {    // SAME lock as above! Even though count2 is independent
            count2++;            // Thread must wait even if only count1 is being modified
        }
    }

    // Option B: Separate lock objects — independent locking
    // Better: count1 and count2 can be modified in PARALLEL
    void incrementCount1_separate() {
        synchronized (lock1) {   // Only locks count1's lock
            count1++;
        }
    }
    void incrementCount2_separate() {
        synchronized (lock2) {   // Different lock! Doesn't block count1 operations
            count2++;            // Both can run simultaneously
        }
    }

    // Option C: Class-level lock (for static methods/data)
    private static int staticCount = 0;
    static void incrementStatic() {
        // For static fields, lock on the Class object
        // because there's no 'this' in a static context
        synchronized (L04_Synchronized.class) {
            staticCount++;
        }
    }
    // This is equivalent to: static synchronized void incrementStatic()

    static void demoLockTypes() throws InterruptedException {
        L04_Synchronized obj = new L04_Synchronized();

        // Using separate locks — both can run in parallel
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) obj.incrementCount1_separate();
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) obj.incrementCount2_separate();
        });

        long start = System.currentTimeMillis();
        t1.start(); t2.start(); t1.join(); t2.join();
        long duration = System.currentTimeMillis() - start;

        System.out.println("  count1: " + obj.count1 + ", count2: " + obj.count2);
        System.out.println("  Time with separate locks: " + duration + "ms");
        System.out.println("  (Separate locks = parallel, faster. 'this' lock = sequential, slower)");
    }

    // =====================================================================
    // DEMO 4: Reentrant Nature of synchronized
    // =====================================================================
    // "Reentrant" means: if a thread ALREADY holds a lock, it can
    // enter another synchronized block with the SAME lock without deadlocking.
    //
    // Without reentrancy: method A calls method B, both synchronized on 'this'
    //   → Thread holds lock for A → tries to acquire lock for B → DEADLOCK!
    //   (waiting for itself to release the lock it's holding)
    //
    // With reentrancy (Java's behavior): the thread recognizes it already
    //   holds the lock and enters immediately. No deadlock.

    synchronized void methodA() {
        System.out.println("    Inside methodA, calling methodB...");
        methodB();  // This would DEADLOCK if synchronized wasn't reentrant!
    }

    synchronized void methodB() {
        System.out.println("    Inside methodB (same lock, no deadlock — reentrant!)");
    }

    static void demoReentrant() {
        L04_Synchronized obj = new L04_Synchronized();
        obj.methodA(); // Acquires lock → calls methodB → needs SAME lock → enters (reentrant!)
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== DEMO 1: Synchronized Method (Counter Fix) ===\n");
        demoSynchronizedMethod();

        System.out.println("\n=== DEMO 2: Synchronized Block (Bank Fix) ===\n");
        demoSynchronizedBlock();

        System.out.println("\n=== DEMO 3: Lock Types (this vs separate vs class) ===\n");
        demoLockTypes();

        System.out.println("\n=== DEMO 4: Reentrant Synchronized ===\n");
        demoReentrant();

        System.out.println("\nDone!");
    }
}

/*
 * =====================================================================
 * SUMMARY
 * =====================================================================
 *
 * | Type                    | Locks on            | Use when                        |
 * |-------------------------|---------------------|---------------------------------|
 * | synchronized method     | 'this' (instance)   | Simple, entire method is critical|
 * | static synchronized     | Class object        | Static shared data              |
 * | synchronized(this)      | 'this' (instance)   | Part of method is critical      |
 * | synchronized(lockObj)   | Custom object        | Independent resources need      |
 * |                         |                     | separate locks (best practice)  |
 *
 * KEY RULES:
 * 1. synchronized solves ATOMICITY + VISIBILITY (not just atomicity!)
 *    When a thread exits synchronized, all changes are flushed to main memory.
 *    When a thread enters synchronized, it reads fresh values from main memory.
 *
 * 2. Lock is ALWAYS released — even if exception is thrown (unlike ReentrantLock)
 *
 * 3. synchronized is REENTRANT — same thread can re-acquire the same lock
 *
 * 4. Use the SMALLEST scope possible — don't lock more than needed
 *
 * 5. Prefer separate lock objects for independent resources
 *
 * DOWNSIDES OF SYNCHRONIZED:
 *   - Can't try to acquire lock without blocking (no tryLock)
 *   - Can't interrupt a thread waiting for a lock
 *   - Can't have multiple conditions (only one wait/notify per monitor)
 *   - No fairness control (threads aren't guaranteed FIFO order)
 *   → These limitations are solved by ReentrantLock (Lesson 6)
 *
 * NEXT LESSON: L05_WaitNotify.java — Thread communication with wait/notify
 * =====================================================================
 */

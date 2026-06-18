package lld.concurrency.multithreading.basics.lectures;

import java.util.LinkedList;
import java.util.Queue;

/*
 * =====================================================================
 * LESSON 8: WAIT / NOTIFY — Thread Communication
 * =====================================================================
 *
 * So far we've learned to PROTECT shared state (locks, synchronized).
 * But how do threads TALK to each other?
 *
 * Problem: Producer thread creates data, Consumer thread processes it.
 *   Consumer: "Is data ready?"  → No  → Check again → No → Check again... (wastes CPU!)
 *   This is called BUSY WAITING — the thread keeps looping, burning CPU cycles.
 *
 * Solution: wait() and notify()
 *   Consumer: "No data? I'll SLEEP until someone wakes me up." (wait)
 *   Producer: "Data is ready! WAKE UP the consumer." (notify)
 *
 * Rules:
 *   1. wait(), notify(), notifyAll() can ONLY be called inside synchronized block
 *      (you must hold the object's monitor/lock to call these)
 *   2. wait() RELEASES the lock and puts thread to sleep
 *      (unlike Thread.sleep() which holds the lock!)
 *   3. notify() wakes ONE waiting thread
 *   4. notifyAll() wakes ALL waiting threads
 *   5. Always use wait() inside a WHILE loop, not if
 *      (to handle "spurious wakeups" — thread can wake up without notify)
 *
 * Analogy:
 *   Restaurant kitchen:
 *   Waiter (consumer):  "No orders? I'll wait in the break room." (wait)
 *   Chef (producer):    "Order ready! Ring the bell!" (notify)
 *   Waiter wakes up, picks up the order, serves it.
 * =====================================================================
 */
public class L08_WaitNotify {

    // =====================================================================
    // DEMO 1: Basic wait/notify — one thread signals another
    // =====================================================================
    private static final Object signal = new Object();
    private static boolean dataReady = false;

    static void demoBasicWaitNotify() throws InterruptedException {
        Thread consumer = new Thread(() -> {
            synchronized (signal) {
                System.out.println("  Consumer: Waiting for data...");
                // WHILE loop, not IF — protects against spurious wakeups
                // Spurious wakeup: thread wakes up without notify() being called
                // (rare but possible — JVM allows it for performance reasons)
                while (!dataReady) {
                    try {
                        signal.wait();
                        // wait() does THREE things:
                        //   1. RELEASES the lock on 'signal' (other threads can enter)
                        //   2. Puts this thread to SLEEP (WAITING state)
                        //   3. When notified, REACQUIRES the lock before continuing
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                System.out.println("  Consumer: Data received! Processing...");
            }
        });

        Thread producer = new Thread(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            synchronized (signal) {
                System.out.println("  Producer: Data produced! Notifying...");
                dataReady = true;
                signal.notify();
                // notify() wakes up ONE thread waiting on 'signal'
                // The woken thread can't run until we EXIT this synchronized block
                // (because it needs to reacquire the lock)
            }
        });

        dataReady = false;
        consumer.start();
        producer.start();
        consumer.join();
        producer.join();
    }

    // =====================================================================
    // DEMO 2: Producer-Consumer with shared buffer (Queue)
    // =====================================================================
    // Classic interview problem!
    // Producer adds items to a queue. Consumer removes them.
    // Queue has MAX size — producer waits when full, consumer waits when empty.

    private static final int MAX_SIZE = 5;
    private static final Queue<Integer> buffer = new LinkedList<>();

    static void demoProducerConsumer() throws InterruptedException {
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                synchronized (buffer) {
                    // Wait while buffer is FULL
                    while (buffer.size() == MAX_SIZE) {
                        try {
                            System.out.println("  Producer: Buffer full, waiting...");
                            buffer.wait(); // Release lock and wait
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    buffer.add(i);
                    System.out.println("  Producer: Added " + i + " | Buffer: " + buffer);
                    buffer.notifyAll(); // Wake up consumer (buffer not empty anymore)
                }
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            int consumed = 0;
            while (consumed < 10) {
                synchronized (buffer) {
                    // Wait while buffer is EMPTY
                    while (buffer.isEmpty()) {
                        try {
                            System.out.println("  Consumer: Buffer empty, waiting...");
                            buffer.wait(); // Release lock and wait
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    int item = buffer.poll();
                    consumed++;
                    System.out.println("  Consumer: Took " + item + "  | Buffer: " + buffer);
                    buffer.notifyAll(); // Wake up producer (buffer not full anymore)
                }
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Consumer");

        buffer.clear();
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }

    // =====================================================================
    // DEMO 3: notify() vs notifyAll()
    // =====================================================================
    // notify()    → wakes ONE random waiting thread
    // notifyAll() → wakes ALL waiting threads (they compete for the lock)
    //
    // When to use which?
    //   notify():    when ANY one waiter can handle the signal (e.g., one task to do)
    //   notifyAll(): when SPECIFIC waiters need to check condition (e.g., producer-consumer)
    //               or when different threads wait for different conditions on same lock
    //
    // SAFER to always use notifyAll() — notify() can cause missed signals:
    //   Thread A waits for condition X
    //   Thread B waits for condition Y
    //   notify() wakes Thread A, but only condition Y is true
    //   Thread A checks, goes back to wait. Thread B never wakes up!
    //   notifyAll() wakes both — each checks its own condition.

    private static final Object sharedLock = new Object();
    private static int sharedValue = 0;

    static void demoNotifyVsNotifyAll() throws InterruptedException {
        // 3 threads all waiting on the same lock, for different values
        Thread[] waiters = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int targetValue = i + 1;
            waiters[i] = new Thread(() -> {
                synchronized (sharedLock) {
                    while (sharedValue != targetValue) {
                        try {
                            sharedLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    System.out.println("  Waiter-" + targetValue + ": My value " + targetValue + " is set!");
                }
            }, "Waiter-" + (i + 1));
            waiters[i].start();
        }

        Thread.sleep(100); // Let all waiters start

        // Set values one by one, using notifyAll() each time
        for (int val = 1; val <= 3; val++) {
            synchronized (sharedLock) {
                sharedValue = val;
                System.out.println("  Setter: Set value to " + val + ", notifying all...");
                sharedLock.notifyAll(); // ALL waiters wake up, check their condition
                // Only the one whose targetValue matches will proceed
                // Others go back to wait()
            }
            Thread.sleep(100);
        }

        for (Thread t : waiters) t.join();
    }

    // =====================================================================
    // DEMO 4: wait() vs sleep() — KEY DIFFERENCE
    // =====================================================================
    /*
     * | Feature          | wait()                    | sleep()               |
     * |------------------|---------------------------|-----------------------|
     * | Releases lock?   | YES                       | NO                    |
     * | Called on         | Object (the lock)         | Thread class          |
     * | Must be in sync? | YES (synchronized block)  | NO (anywhere)         |
     * | Woken by         | notify()/notifyAll()      | Time expires          |
     * | Purpose          | Thread communication      | Pause execution       |
     *
     * CRITICAL: wait() releases the lock so other threads can work!
     *   synchronized(lock) {
     *       lock.wait();   // releases lock → other threads can enter synchronized
     *   }
     *
     *   synchronized(lock) {
     *       Thread.sleep(1000);  // holds lock → other threads BLOCKED for 1 second!
     *   }
     */

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== DEMO 1: Basic wait/notify ===\n");
        demoBasicWaitNotify();

        System.out.println("\n=== DEMO 2: Producer-Consumer ===\n");
        demoProducerConsumer();

        System.out.println("\n=== DEMO 3: notify vs notifyAll ===\n");
        demoNotifyVsNotifyAll();

        System.out.println("\nDone!");
    }
}

/*
 * =====================================================================
 * SUMMARY
 * =====================================================================
 *
 * wait/notify is the OLDEST way to do thread communication in Java.
 * It works but has limitations:
 *   - Must be inside synchronized (easy to forget → IllegalMonitorStateException)
 *   - Only ONE wait queue per object (can't signal specific threads)
 *   - Easy to get wrong (spurious wakeups, missed signals)
 *
 * Modern alternatives (covered in next lessons):
 *   - Condition (Lock's version of wait/notify — multiple queues, more control)
 *   - BlockingQueue (handles producer-consumer automatically)
 *   - CountDownLatch, CyclicBarrier (specialized coordination)
 *
 * KEY RULES:
 *   1. Always call wait() inside a WHILE loop (not if)
 *   2. Always call wait/notify inside synchronized on the SAME object
 *   3. Prefer notifyAll() over notify() (safer, avoids missed signals)
 *   4. wait() releases the lock, sleep() does NOT
 *
 * NEXT LESSON: L09_Condition.java — ReentrantLock's wait/notify
 * =====================================================================
 */

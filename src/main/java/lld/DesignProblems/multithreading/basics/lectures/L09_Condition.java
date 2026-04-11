package lld.DesignProblems.multithreading.basics.lectures;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
 * =====================================================================
 * LESSON 9: CONDITION — ReentrantLock's wait/notify
 * =====================================================================
 *
 * In Lesson 8 we used wait/notify with synchronized.
 * Problem: only ONE wait queue per object — can't signal specific threads.
 *
 * Condition = the ReentrantLock version of wait/notify.
 * Advantage: you can create MULTIPLE Conditions from one lock,
 *   each with its own waiting queue. Signal exactly the right thread!
 *
 * Mapping:
 *   | synchronized      | ReentrantLock + Condition |
 *   |-------------------|--------------------------|
 *   | synchronized(obj) | lock.lock()              |
 *   | obj.wait()        | condition.await()        |
 *   | obj.notify()      | condition.signal()       |
 *   | obj.notifyAll()   | condition.signalAll()    |
 *
 * Just like wait/notify, await() RELEASES the lock and sleeps.
 * signal() wakes a thread, which reacquires the lock before continuing.
 * =====================================================================
 */
public class L09_Condition {

    // =====================================================================
    // DEMO 1: Producer-Consumer with Condition (compare with L08 Demo 2)
    // =====================================================================
    // Two separate conditions:
    //   notFull  → producer waits here when buffer is full
    //   notEmpty → consumer waits here when buffer is empty
    //
    // With wait/notify, both producer and consumer wait on the SAME queue.
    // notifyAll() wakes BOTH — wasteful. With Conditions, we wake only the right one.

    private static final int MAX_SIZE = 5;
    private static final Queue<Integer> buffer = new LinkedList<>();
    private static final ReentrantLock bufferLock = new ReentrantLock();
    private static final Condition notFull = bufferLock.newCondition();  // Producer waits here
    private static final Condition notEmpty = bufferLock.newCondition(); // Consumer waits here

    static void demoProducerConsumer() throws InterruptedException {
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                bufferLock.lock();
                try {
                    // Wait while buffer is full
                    while (buffer.size() == MAX_SIZE) {
                        System.out.println("  Producer: Buffer full, waiting on 'notFull'...");
                        notFull.await(); // Releases lock, sleeps on notFull's queue
                    }
                    buffer.add(i);
                    System.out.println("  Producer: Added " + i + " | Buffer: " + buffer);
                    notEmpty.signal(); // Wake consumer: "buffer has data now!"
                    // signal() on notEmpty wakes ONLY threads waiting on notEmpty
                    // (i.e., the consumer). Producer threads on notFull are NOT woken.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } finally {
                    bufferLock.unlock();
                }
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            int consumed = 0;
            while (consumed < 10) {
                bufferLock.lock();
                try {
                    while (buffer.isEmpty()) {
                        System.out.println("  Consumer: Buffer empty, waiting on 'notEmpty'...");
                        notEmpty.await(); // Releases lock, sleeps on notEmpty's queue
                    }
                    int item = buffer.poll();
                    consumed++;
                    System.out.println("  Consumer: Took " + item + "  | Buffer: " + buffer);
                    notFull.signal(); // Wake producer: "buffer has space now!"
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } finally {
                    bufferLock.unlock();
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
    // DEMO 2: Multiple Conditions — Print A, B, C in order using 3 threads
    // =====================================================================
    // Each thread has its OWN condition — we signal exactly which thread to wake.
    // This is IMPOSSIBLE with plain wait/notify (only one queue per monitor).

    private static final ReentrantLock printLock = new ReentrantLock();
    private static final Condition turnA = printLock.newCondition();
    private static final Condition turnB = printLock.newCondition();
    private static final Condition turnC = printLock.newCondition();
    private static int currentTurn = 0; // 0=A, 1=B, 2=C

    static void demoOrderedPrinting() throws InterruptedException {
        int rounds = 3; // Print ABC 3 times

        Thread threadA = new Thread(() -> {
            for (int i = 0; i < rounds; i++) {
                printLock.lock();
                try {
                    while (currentTurn != 0) turnA.await(); // Wait for my turn
                    System.out.print("A");
                    currentTurn = 1;
                    turnB.signal(); // Wake ONLY thread B
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    printLock.unlock();
                }
            }
        });

        Thread threadB = new Thread(() -> {
            for (int i = 0; i < rounds; i++) {
                printLock.lock();
                try {
                    while (currentTurn != 1) turnB.await();
                    System.out.print("B");
                    currentTurn = 2;
                    turnC.signal(); // Wake ONLY thread C
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    printLock.unlock();
                }
            }
        });

        Thread threadC = new Thread(() -> {
            for (int i = 0; i < rounds; i++) {
                printLock.lock();
                try {
                    while (currentTurn != 2) turnC.await();
                    System.out.print("C ");
                    currentTurn = 0;
                    turnA.signal(); // Wake ONLY thread A — back to start
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    printLock.unlock();
                }
            }
        });

        currentTurn = 0;
        threadA.start(); threadB.start(); threadC.start();
        threadA.join(); threadB.join(); threadC.join();
        System.out.println();
    }

    // =====================================================================
    // DEMO 3: await with timeout — don't wait forever
    // =====================================================================
    static void demoAwaitTimeout() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Condition dataReady = lock.newCondition();

        Thread waiter = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("  Waiter: Waiting for data (max 500ms)...");
                // await(time, unit) returns false if timeout expired (no signal received)
                // await(time, unit) returns true if it was signaled before timeout
                boolean signaled = dataReady.await(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (signaled) {
                    System.out.println("  Waiter: Got signal! Processing data.");
                } else {
                    System.out.println("  Waiter: Timeout! No data received in 500ms.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });

        waiter.start();
        // Don't signal — let it timeout
        waiter.join();
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== DEMO 1: Producer-Consumer with Conditions ===\n");
        demoProducerConsumer();

        System.out.println("\n=== DEMO 2: Ordered Printing (ABC) with 3 Conditions ===\n");
        demoOrderedPrinting();

        System.out.println("\n=== DEMO 3: await with Timeout ===\n");
        demoAwaitTimeout();

        System.out.println("\nDone!");
    }
}

/*
 * =====================================================================
 * SUMMARY: wait/notify vs Condition
 * =====================================================================
 *
 * | Feature                | wait/notify (L08)   | Condition (L09)        |
 * |------------------------|---------------------|------------------------|
 * | Used with              | synchronized        | ReentrantLock          |
 * | Wait queues            | ONE per object      | MULTIPLE per lock      |
 * | Targeted signaling     | NO (notify is random)| YES (signal specific)  |
 * | Timeout support        | wait(ms) — basic    | await(time, unit) — rich|
 * | Interruptible          | YES                 | YES + awaitUninterruptibly()|
 * | Returns info           | void                | boolean (timed await)  |
 *
 * WHEN TO USE:
 *   wait/notify:  Simple cases, already using synchronized
 *   Condition:    Need multiple queues, targeted signaling, or using ReentrantLock
 *
 * NEXT LESSON: L10_Synchronizers.java — Semaphore, CountDownLatch, CyclicBarrier
 * =====================================================================
 */

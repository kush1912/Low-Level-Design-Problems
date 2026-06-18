package lld.concurrency.multithreading.basics;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class PrintEvenOdd {
    // final: set once in constructor, never modified. JMM guarantees visibility to all threads.
    private final int limit;
    // Not AtomicInteger because it's already protected by the ReentrantLock.
    // All reads, checks, and writes happen inside lock.lock()/unlock() — making it
    // AtomicInteger would be double protection (unnecessary overhead from volatile reads).
    // Rule: using a Lock? → plain int. No lock? → AtomicInteger.
    private int counter = 1;


    private final ReentrantLock lock = new ReentrantLock();

    // Separate Conditions allow targeted signaling — wake up exactly the right thread.
    // signal() wakes ONE thread waiting on that Condition. signalAll() wakes ALL.
    // signal() is the ReentrantLock equivalent of notify(), but better — because you have
    // separate Conditions (oddTurn, evenTurn), you wake up exactly the right thread.
    // With notify() on a single monitor, you can't target which thread to wake.
    //
    // Condition is an interface from java.util.concurrent.locks package.
    // lock.newCondition() creates a Condition object BOUND to this specific lock.
    // Each Condition maintains its own waiting queue of threads.
    // - oddTurn.await()  → puts current thread into oddTurn's waiting queue
    // - evenTurn.signal() → wakes a thread from evenTurn's waiting queue
    // So when OddThread calls evenTurn.signal(), it specifically wakes the thread
    // that called evenTurn.await() (EvenThread) — not any random thread.
    // This is like having separate "waiting rooms" for each thread role.
    private final Condition oddTurn = lock.newCondition();
    private final Condition evenTurn = lock.newCondition();

    public PrintEvenOdd(int limit) {
        this.limit = limit;
    }

    private void printOdd() {
        while (counter <= limit) {
            lock.lock();
            try {
                while (counter <= limit && counter % 2 == 0) {
                    oddTurn.await();
                }
                if (counter > limit) break;
                System.out.println(Thread.currentThread().getName() + " - " + counter);
                counter++;
                evenTurn.signal(); // Wake EvenThread — "it's your turn now"
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }
        }
    }

    private void printEven() {
        while (counter <= limit) {
            lock.lock();
            try {
                while (counter <= limit && counter % 2 != 0) {
                    evenTurn.await();
                }
                if (counter > limit) break;
                System.out.println(Thread.currentThread().getName() + " - " + counter);
                counter++;
                oddTurn.signal(); // Wake OddThread — "it's your turn now"  // wakes threads waiting on oddTurn (only OddThread)
                // So even if you call evenTurn.signalAll(), it only wakes threads that called evenTurn.await(). It has zero effect on threads waiting on oddTurn
                //That's the whole advantage of separate Conditions — each one is an isolated waiting queue. signalAll() means "wake all in this queue", not "wake all
                //threads everywhere
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        PrintEvenOdd printEvenOdd = new PrintEvenOdd(10);

        // Method reference: printEvenOdd::printOdd is shorthand for () -> printEvenOdd.printOdd()
        // Works because printOdd() matches Runnable's functional interface: no args, void return
        // Equivalent to: new Thread(() -> printEvenOdd.printOdd(), "OddThread")
        Thread t1 = new Thread(printEvenOdd::printOdd, "OddThread");
        Thread t2 = new Thread(printEvenOdd::printEven, "EvenThread");

        t1.start();
        t2.start();

        /*
        t1.join() tells the calling thread (here main) to wait until t1 finishes before proceeding.
        Without it:
         main()  →  starts t1  →  starts t2  →  prints "Done!"  →  exits
         // t1 and t2 might still be running when "Done!" prints

        With join():
         main()  →  starts t1  →  starts t2  →  waits for t1  →  waits for t2  →  prints "Done!"
         // Guaranteed: "Done!" prints only after both threads complete
        It's essential for graceful shutdown — ensuring all work is finished before the program ends or moves to the next step
        * */
        t1.join();
        t2.join();
        System.out.println("Done!");
    }
}

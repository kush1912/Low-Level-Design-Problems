package lld.DesignProblems.multithreading.basics.lectures;

/*
 * =====================================================================
 * LESSON 1: CREATING THREADS IN JAVA — 3 WAYS
 * =====================================================================
 *
 * What is a Thread?
 *   A thread is an independent path of execution in your program.
 *   By default, Java runs on a single thread called "main".
 *   Creating more threads lets you do multiple things at the same time.
 *
 * 3 ways to create threads (oldest → newest):
 *   Way 1: Extend Thread class         (Java 1.0)
 *   Way 2: Implement Runnable interface (Java 1.0, preferred)
 *   Way 3: Lambda expression            (Java 8+, most concise)
 * =====================================================================
 */
public class L01_CreatingThreads {

    // =====================================================================
    // WAY 1: Extend Thread class
    // =====================================================================
    // Simple but inflexible — Java doesn't allow multiple inheritance,
    // so if your class extends Thread, it can't extend anything else.
    // Use this only for simple, throwaway thread tasks.
    static class MyThread extends Thread {
        @Override
        public void run() {
            // This code runs on a NEW thread (not main)
            System.out.println("Way 1: Running on " + Thread.currentThread().getName());
        }
    }

    // =====================================================================
    // WAY 2: Implement Runnable interface
    // =====================================================================
    // Preferred over Way 1 because:
    //   - Your class can still extend another class
    //   - Separates "what to do" (task) from "how to run" (thread)
    //   - Same Runnable can be reused with different threads or thread pools
    //
    // Runnable is a functional interface with one method: void run()
    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("Way 2: Running on " + Thread.currentThread().getName());
        }
    }

    public static void main(String[] args) {

        // --- Way 1: Extend Thread ---
        // Create thread object and start it
        // start() creates a new OS thread and calls run() on it
        // NEVER call run() directly — that runs on the SAME thread (main), not a new one!
        MyThread t1 = new MyThread();
        t1.setName("Thread-Way1");
        t1.start();  // start() → new thread → calls run()
        // t1.run(); // WRONG! This would run on main thread, NOT a new thread
        //
        // run() "works" but look at the thread name — it says "main", not "Thread-Way1"
        // start() → OS creates new thread → calls run() on it     → PARALLEL execution
        // run()   → directly executes on current thread (main)     → SEQUENTIAL execution
        //
        // Example to see the difference:
        //   t.run()   → Before → (3 sec wait) → Finished → After  (main blocked)
        //   t.start() → Before → After → (3 sec wait) → Finished  (main continues)
        // run() defeats the whole purpose of threading.

        // --- Way 2: Implement Runnable ---
        // Pass Runnable to Thread constructor
        // Thread is the "vehicle", Runnable is the "task"
        MyRunnable task = new MyRunnable();
        Thread t2 = new Thread(task, "Thread-Way2");
        t2.start();

        // --- Way 3: Lambda (Java 8+) ---
        // Since Runnable has only ONE method (run), we can use a lambda
        // This is the most concise and modern way
        // () -> { ... } means "no arguments, execute this code"
        Thread t3 = new Thread(() -> {
            System.out.println("Way 3: Running on " + Thread.currentThread().getName());
        }, "Thread-Way3");
        t3.run();

        // --- Way 3b: Method reference (even shorter) ---
        // If the task is just calling a single method, use ::
        Thread t4 = new Thread(L01_CreatingThreads::doWork, "Thread-Way3b");
        t4.start();

        // This prints on the MAIN thread
        System.out.println("Main: Running on " + Thread.currentThread().getName());

        /*
         * NOTE: The output order is UNPREDICTABLE!
         * You might see:
         *   Way 1: Running on Thread-Way1
         *   Main: Running on main
         *   Way 3: Running on Thread-Way3
         *   Way 2: Running on Thread-Way2
         *
         * This is because the OS thread scheduler decides
         * which thread runs when. You have NO control over this.
         */
    }

    // A simple method used for Way 3b (method reference)
    // This method MUST have no params and return void — because Runnable's run() is: void run()
    // A method reference must match the functional interface signature.
    //
    // What if you need to pass data to a thread?
    //   Option 1: Lambda (capture variables):
    //       String name = "Ajay";
    //       new Thread(() -> doWork(name));  // lambda wraps the call with params
    //
    //   Option 2: Callable<T> (returns a value, used with ExecutorService — Lesson 8):
    //       Callable<String> task = () -> fetchData("user123");
    //
    //   Option 3: Pass data via constructor:
    //       class MyTask implements Runnable {
    //           private final String name;
    //           MyTask(String name) { this.name = name; }
    //           public void run() { System.out.println("Hello " + name); }
    //       }
    //
    // TLDR: Runnable = no params, no return. Need params? Wrap in lambda.
    //        Need return value? Use Callable (Lesson 8).
    static void doWork() {
        System.out.println("Way 3b: Running on " + Thread.currentThread().getName());
    }
}
// TLDR: Runnable = no params, no return. Need params? Wrap in a lambda. Need return value? Use Callable. We'll cover Callable in Lesson 8 (Executors).

/*
 * =====================================================================
 * SUMMARY
 * =====================================================================
 *
 * | Way          | Syntax                          | When to use          |
 * |--------------|---------------------------------|----------------------|
 * | extends Thread | class X extends Thread         | Quick & simple tasks |
 * | Runnable     | class X implements Runnable      | Need to extend other class |
 * | Lambda       | new Thread(() -> { ... })        | Most cases (Java 8+) |
 * | Method Ref   | new Thread(Class::method)        | Single method call   |
 *
 * KEY RULES:
 * 1. Always call start(), NEVER run() — run() doesn't create a new thread
 * 2. Prefer Runnable/Lambda over extending Thread
 * 3. Thread.currentThread().getName() tells you which thread is running
 * 4. Output order is NEVER guaranteed — OS scheduler decides
 *
 * NEXT LESSON: L02_ThreadLifecycle.java — sleep, join, interrupt, thread states
 * =====================================================================
 */

package lld.DesignProblems.multithreading.basics.lectures;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

/*
 * =====================================================================
 * LESSON 11: THREAD POOLS & EXECUTORS
 * =====================================================================
 *
 * Problem: Creating a new Thread for every task is EXPENSIVE.
 *   Thread creation = OS call, memory allocation (~1MB stack per thread).
 *   1000 tasks = 1000 threads = 1GB memory + massive context switching!
 *
 * Solution: Thread Pool — a pool of REUSABLE worker threads.
 *   Create N threads once. Submit tasks to a queue.
 *   Idle workers pick up tasks from the queue. No thread creation overhead!
 *
 * Analogy:
 *   Without pool: Hire a new employee for every task, fire them when done.
 *   With pool: Have 10 permanent employees who pick up tasks from a to-do list.
 *
 * Java provides the Executor framework (java.util.concurrent):
 *   Executor           → submit tasks
 *   ExecutorService     → submit + lifecycle management (shutdown)
 *   Executors           → factory methods to create common pools
 *   Future              → get result from async task
 *   CompletableFuture   → modern async programming (callbacks, chaining)
 * =====================================================================
 */
public class L11_ThreadPools {

    // =====================================================================
    // DEMO 1: FixedThreadPool — N worker threads, shared task queue
    // =====================================================================
    // Creates exactly N threads. Tasks queue up if all threads are busy.
    //
    // Use when: you know the optimal number of threads
    //   CPU-bound tasks: N = number of CPU cores
    //   IO-bound tasks:  N = cores * 2 (or more, since threads wait on IO)

    static void demoFixedThreadPool() throws InterruptedException {
        // Pool of 3 threads handling 6 tasks
        ExecutorService pool = Executors.newFixedThreadPool(3);

        for (int i = 1; i <= 6; i++) {
            final int taskId = i;
            pool.submit(() -> {
                // submit() vs execute():
                //   execute(Runnable) → fire and forget, no return value
                //   submit(Runnable)  → returns Future (can check completion/errors)
                //   submit(Callable)  → returns Future with result
                System.out.println("  Task " + taskId + " running on " + Thread.currentThread().getName());
                try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }

        pool.shutdown(); // No new tasks accepted, finish existing ones
        // shutdown() vs shutdownNow():
        //   shutdown():    graceful — waits for running tasks to finish
        //   shutdownNow(): forceful — interrupts running tasks, returns queued ones
        pool.awaitTermination(5, TimeUnit.SECONDS); // Wait for all tasks to complete
        System.out.println("  All tasks completed.");
    }

    // =====================================================================
    // DEMO 2: CachedThreadPool — Creates threads on demand, reuses idle ones
    // =====================================================================
    // Creates new threads as needed. Idle threads are reused for 60 seconds,
    // then removed. Pool can grow UNBOUNDED — be careful!
    //
    // Use when: many short-lived tasks with unpredictable load
    // DANGER: if tasks are slow, pool grows without limit → OutOfMemoryError!

    static void demoCachedThreadPool() throws InterruptedException {
        ExecutorService pool = Executors.newCachedThreadPool();

        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            pool.submit(() -> {
                System.out.println("  Task " + taskId + " on " + Thread.currentThread().getName());
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        // Notice: thread names might be reused (pool-X-thread-1 appears multiple times)
    }

    // =====================================================================
    // DEMO 3: SingleThreadExecutor — One thread, tasks execute sequentially
    // =====================================================================
    // Guarantees tasks run in ORDER (FIFO). Only one task at a time.
    //
    // Use when: tasks must be sequential (logging, event processing)

    static void demoSingleThread() throws InterruptedException {
        ExecutorService pool = Executors.newSingleThreadExecutor();

        for (int i = 1; i <= 4; i++) {
            final int taskId = i;
            pool.submit(() -> {
                System.out.println("  Task " + taskId + " on " + Thread.currentThread().getName());
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("  (Tasks always execute in order: 1, 2, 3, 4)");
    }

    // =====================================================================
    // DEMO 4: ScheduledThreadPool — Run tasks at fixed intervals or delays
    // =====================================================================
    // Schedule tasks to run after a delay, or periodically.
    //
    // Use when: recurring tasks (cleanup, health checks, polling)

    static void demoScheduledPool() throws InterruptedException {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);

        // Run once after 500ms delay
        pool.schedule(() -> {
            System.out.println("  Delayed task ran after 500ms");
        }, 500, TimeUnit.MILLISECONDS);

        // Run periodically: first run after 100ms, then every 300ms
        ScheduledFuture<?> periodic = pool.scheduleAtFixedRate(() -> {
            System.out.println("  Periodic task at " + System.currentTimeMillis() % 10000 + "ms");
        }, 100, 300, TimeUnit.MILLISECONDS);

        // Let it run a few times
        Thread.sleep(1200);
        periodic.cancel(false); // Stop the periodic task

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);
    }

    // =====================================================================
    // DEMO 5: Callable + Future — Getting results from threads
    // =====================================================================
    // Runnable: void run()    → no return value, can't throw checked exceptions
    // Callable: T call()      → returns a value, can throw exceptions
    //
    // Future: represents the result of an async computation
    //   future.get()          → blocks until result is ready
    //   future.get(timeout)   → blocks with timeout
    //   future.isDone()       → check if completed
    //   future.cancel()       → cancel the task

    static void demoCallableFuture() throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(3);

        // Submit Callable tasks that return results
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            Future<String> future = pool.submit(() -> {
                // Callable<String> — returns String, can throw Exception
                Thread.sleep(taskId * 200);
                return "Result from task " + taskId;
            });
            futures.add(future);
        }

        // Collect results
        for (Future<String> future : futures) {
            String result = future.get(); // Blocks until this task completes
            System.out.println("  " + result);
        }

        pool.shutdown();
    }

    // =====================================================================
    // DEMO 6: CompletableFuture — Modern async programming
    // =====================================================================
    // Future.get() blocks. CompletableFuture is non-blocking with callbacks!
    //
    // Chain operations: fetchUser → getOrders → calculateTotal
    // Without blocking the main thread!

    static void demoCompletableFuture() throws InterruptedException {
        // supplyAsync: runs on ForkJoinPool.commonPool() by default
        CompletableFuture<String> future = CompletableFuture
            .supplyAsync(() -> {
                // Step 1: Fetch user (runs on pool thread)
                System.out.println("  Fetching user on " + Thread.currentThread().getName());
                try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "User: Ajay";
            })
            .thenApply(user -> {
                // Step 2: Get orders (runs after step 1 completes)
                System.out.println("  Getting orders for " + user);
                return user + " | Orders: 3";
            })
            .thenApply(data -> {
                // Step 3: Calculate total
                System.out.println("  Calculating total for " + data);
                return data + " | Total: $150";
            });

        // None of the above blocked the main thread!
        System.out.println("  Main thread is FREE while async work happens...");

        // Get final result (blocks only when we actually need it)
        String result = future.join(); // join() is like get() but doesn't throw checked exception
        System.out.println("  Final: " + result);

        // Combine two independent async operations
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "Ajay";
        });
        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "3 items";
        });

        // thenCombine: run both in PARALLEL, combine results when both done
        String combined = userFuture.thenCombine(orderFuture, (user, orders) ->
            user + " has " + orders
        ).join();
        System.out.println("  Combined: " + combined);
    }

    // =====================================================================
    // DEMO 7: ThreadPoolExecutor — Custom thread pool (interview favorite!)
    // =====================================================================
    // Executors.newFixedThreadPool() etc. are convenience wrappers.
    // ThreadPoolExecutor gives you FULL control over all parameters.

    static void demoCustomPool() throws InterruptedException {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            2,                          // corePoolSize: always keep 2 threads alive
            4,                          // maxPoolSize: can grow up to 4 under load
            60, TimeUnit.SECONDS,       // keepAliveTime: idle threads beyond core die after 60s
            new ArrayBlockingQueue<>(2), // workQueue: holds 2 pending tasks
            // Rejection handler: what to do when queue is full AND max threads are busy
            new ThreadPoolExecutor.CallerRunsPolicy()
            // CallerRunsPolicy: submitter thread runs the task itself (backpressure!)
            //
            // Other rejection policies:
            //   AbortPolicy (default): throws RejectedExecutionException
            //   DiscardPolicy:         silently drops the task
            //   DiscardOldestPolicy:   drops oldest queued task, adds new one
        );

        /*
         * How tasks flow in ThreadPoolExecutor:
         *
         *   New task arrives
         *       ↓
         *   Core threads busy? (< corePoolSize)
         *   NO → create new core thread to run it
         *   YES ↓
         *   Queue has space?
         *   YES → add to queue (wait for a thread)
         *   NO ↓
         *   Max threads reached? (>= maxPoolSize)
         *   NO → create new thread (temporary, dies after keepAliveTime)
         *   YES ↓
         *   Rejection handler kicks in! (CallerRunsPolicy, AbortPolicy, etc.)
         */

        System.out.println("  Pool: core=2, max=4, queue=2");

        for (int i = 1; i <= 8; i++) {
            final int taskId = i;
            System.out.println("  Submitting task " + taskId + "...");
            pool.submit(() -> {
                System.out.println("    Task " + taskId + " on " + Thread.currentThread().getName());
                try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("  Custom pool completed.");
    }

    public static void main(String[] args) throws Exception {

        System.out.println("=== DEMO 1: FixedThreadPool ===\n");
        demoFixedThreadPool();

        System.out.println("\n=== DEMO 2: CachedThreadPool ===\n");
        demoCachedThreadPool();

        System.out.println("\n=== DEMO 3: SingleThreadExecutor ===\n");
        demoSingleThread();

        System.out.println("\n=== DEMO 4: ScheduledThreadPool ===\n");
        demoScheduledPool();

        System.out.println("\n=== DEMO 5: Callable + Future ===\n");
        demoCallableFuture();

        System.out.println("\n=== DEMO 6: CompletableFuture ===\n");
        demoCompletableFuture();

        System.out.println("\n=== DEMO 7: Custom ThreadPoolExecutor ===\n");
        demoCustomPool();

        System.out.println("\nDone! All lessons complete.");
    }
}

/*
 * =====================================================================
 * SUMMARY: CHOOSING THE RIGHT POOL
 * =====================================================================
 *
 * | Pool Type            | Threads | Queue     | Use when                     |
 * |----------------------|---------|-----------|------------------------------|
 * | FixedThreadPool(N)   | N fixed | Unbounded | Known workload, predictable  |
 * | CachedThreadPool     | 0-INF   | None      | Many short tasks, bursty     |
 * | SingleThreadExecutor | 1       | Unbounded | Sequential task execution    |
 * | ScheduledThreadPool  | N fixed | Delayed   | Periodic/delayed tasks       |
 * | Custom (TPE)         | Custom  | Custom    | Full control needed          |
 * | ForkJoinPool         | N cores | Work-steal| Recursive divide-and-conquer |
 *
 * KEY FORMULAS:
 *   CPU-bound tasks: threads = number of CPU cores
 *   IO-bound tasks:  threads = cores * (1 + wait_time / compute_time)
 *
 * ALWAYS:
 *   - Call shutdown() when done (or use try-with-resources in Java 19+)
 *   - Handle exceptions in tasks (they're swallowed silently in pools!)
 *   - Use bounded queues in production (unbounded = OutOfMemoryError risk)
 *
 * =====================================================================
 * CONGRATULATIONS! You've completed all 11 lessons.
 *
 * RECAP:
 *   L01: Creating Threads (Thread, Runnable, Lambda)
 *   L02: Thread Lifecycle (sleep, join, interrupt, daemon)
 *   L03: Race Conditions (the problem)
 *   L04: synchronized (solution 1)
 *   L05: volatile (solution 2 — visibility)
 *   L06: AtomicInteger/CAS (solution 3 — lock-free)
 *   L07: ReentrantLock (solution 4 — advanced locking)
 *   L08: wait/notify (thread communication)
 *   L09: Condition (Lock's wait/notify)
 *   L10: Synchronizers (Semaphore, CountDownLatch, CyclicBarrier)
 *   L11: Thread Pools & Executors (production-ready threading)
 *
 * NOW PRACTICE: Go back to the multithreading problems in the
 *   LLD_Multithreading_Interview_Questions.txt and implement them!
 * =====================================================================
 */

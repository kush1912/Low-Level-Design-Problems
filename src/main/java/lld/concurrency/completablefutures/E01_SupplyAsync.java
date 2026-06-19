package lld.concurrency.completablefutures;

import lld.concurrency.completablefutures.UserDB.UserProfile;
import java.util.concurrent.CompletableFuture;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * EXERCISE 1: supplyAsync — Fetch User Profile from Database
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * SCENARIO:
 * You're building a user dashboard service. When a user logs in, you need to
 * fetch their profile from the database. This is an I/O operation that takes
 * ~200ms. You don't want to block the main thread while waiting.
 *
 * PROBLEM:
 * Implement `fetchUserProfile(String userId)` that:
 *   1. Returns a CompletableFuture<UserProfile>
 *   2. The actual DB fetch should happen on a background thread
 *   3. Use db.getUserById(userId) for the actual fetch
 *
 * WHAT YOU NEED TO FIGURE OUT:
 *   - How do you run something in background and get a future result?
 *   - What's the difference between runAsync and supplyAsync?
 *
 * TEST YOUR SOLUTION:
 *   Run main() — it should print the user profile without blocking.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class E01_SupplyAsync {

    private static final UserDB db = new UserDB();

    public static void main(String[] args) throws Exception {
        System.out.println("Fetching user profile Sync...");
        long start = System.currentTimeMillis();
        UserProfile user = db.getUserById("U-1001");
        System.out.println("Request Sent!");
        for(int i=0; i<=5; i++){
            System.out.println("Doing Work: " + i);
            Thread.sleep(100);
        }
        System.out.println(user);
        System.out.println("Time: " + (System.currentTimeMillis()- start));


        System.out.println("Fetching user profile Async...");
        long st = System.currentTimeMillis();
        CompletableFuture
                .supplyAsync(()->db.getUserById("U-1002"))
                        .thenAccept(userProfile -> System.out.println("Received: "+ userProfile));

//        CompletableFuture<UserProfile> userFuture = CompletableFuture
//                .supplyAsync(()->db.getUserById("U-1002"));
        System.out.println("Request Sent!");
//        System.out.println(userFuture.get());
        for(int i=0; i<=5; i++) {
            System.out.println("Doing Work: " + i);
            Thread.sleep(100);
        }
        System.out.println("Time: " + (System.currentTimeMillis()-st));


        CompletableFuture<UserProfile> future =
                CompletableFuture.supplyAsync(() -> db.getUserById("U-1002"));

        future.thenAccept(userProfile ->
                System.out.println(
                        "Got User: " + user +
                                " on " + Thread.currentThread().getName()));

        for(int i=0;i<10;i++) {
            System.out.println(
                    "Work " + i +
                            " on " + Thread.currentThread().getName());

            Thread.sleep(100);
        }


        // TODO: Uncomment once you implement fetchUserProfile
        // CompletableFuture<UserProfile> future = fetchUserProfile("U-1001");
        //
        // System.out.println("Request sent! Main thread free to do other work...");
        // System.out.println("... doing other work ...");
        //
        // UserProfile profile = future.get(); // Block here to get result
        // System.out.println("Got profile: " + profile);
        // System.out.println("Total time: " + (System.currentTimeMillis() - start) + "ms");
    }
}

package lld.concurrency.completablefutures;

import lld.concurrency.completablefutures.UserDB.UserProfile;
import lld.concurrency.completablefutures.UserDB.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * EXERCISE 3: thenCompose — Chain Dependent Async Calls
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * SCENARIO:
 * When a user views their dashboard, you need to:
 *   1. First fetch their profile (to validate they exist)
 *   2. Then fetch their orders (requires userId from step 1)
 *
 * Both are async DB calls. The second DEPENDS on the first completing.
 *
 * PROBLEM:
 * Implement `fetchUserOrders(String userId)` that:
 *   1. Fetches UserProfile asynchronously
 *   2. Then fetches their orders asynchronously (using the profile's userId)
 *   3. Returns CompletableFuture<List<Order>>
 *
 * WHAT YOU NEED TO FIGURE OUT:
 *   - Why can't you use thenApply here?
 *   - What happens if you use thenApply with a function that returns CompletableFuture?
 *   - How does thenCompose flatten the result?
 *
 * HINT:
 *   thenApply:   CompletableFuture<A> → (A → B)                  → CompletableFuture<B>
 *   thenCompose: CompletableFuture<A> → (A → CompletableFuture<B>) → CompletableFuture<B>
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class E03_ThenCompose {

    private static final UserDB db = new UserDB();

    // ──────────────────────────────────────────────────────────────────────────
    // GIVEN: Async wrappers for DB calls (both return CompletableFuture)
    // ──────────────────────────────────────────────────────────────────────────
    private static CompletableFuture<UserProfile> fetchProfile(String userId) {
        return CompletableFuture.supplyAsync(() -> db.getUserById(userId));
    }

    private static CompletableFuture<List<Order>> fetchOrders(String userId) {
        return CompletableFuture.supplyAsync(() -> db.getOrdersByUserId(userId));
    }


    public static CompletableFuture<List<Order>> fetchUserOrders(String userId) {

        return fetchProfile(userId)
                .thenCompose(profile ->
                        fetchOrders(profile.userId()));
    }

    /**
     * For each user:
     *   Profile (Future<UserProfile>)
     *      -> thenCompose()
     *   Orders (Future<List<Order>>)
     *
     * allOf() waits for all user pipelines to complete.
     * thenApply() collects completed results using join().
     *
     * Why thenCompose?
     *   fetchOrders() returns CompletableFuture<List<Order>>,
     *   so thenApply() would create a nested future.
     *
     * Final Type:
     *   CompletableFuture<List<List<Order>>>
     */


    /**
     * Why allOf() before join()?
     *
     * Directly calling join() blocks the current thread while waiting
     * for each future's result.
     *
     * allOf() creates a single future that completes only when all
     * underlying futures are done, keeping the pipeline asynchronous.
     *
     * After allOf() completes, join() returns immediately because
     * every future is already completed.
     */
    public static CompletableFuture<List<List<Order>>> fetchOrdersForMultipleUsers(
            List<String> userIds) {

        List<CompletableFuture<List<Order>>> futures =
                userIds.stream()
                        .map(userId ->
                                fetchProfile(userId)
                                        .thenCompose(profile ->
                                                fetchOrders(profile.userId())))
                        .toList();

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .toList());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Fetching user orders...\n");

        // TODO: Uncomment once you implement fetchUserOrders
         CompletableFuture<List<Order>> ordersFuture = fetchUserOrders("U-1001");

         List<String> userIds = new ArrayList<>();
         userIds.addAll(Arrays.asList("U-1002", "U-1003"));
         System.out.println("Request sent, main thread free...");

         List<Order> orders = ordersFuture.get();
         System.out.println("Alice's orders:");
         orders.forEach(o -> System.out.println("  " + o));
//         Expected: ORD-001 ($299.99) and ORD-002 ($149.50)
    }
}

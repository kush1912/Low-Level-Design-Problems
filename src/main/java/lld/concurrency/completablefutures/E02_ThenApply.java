package lld.concurrency.completablefutures;

import lld.concurrency.completablefutures.UserDB.UserProfile;
import java.util.concurrent.CompletableFuture;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * EXERCISE 2: thenApply — Transform Async Results
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * SCENARIO:
 * Your dashboard API returns a simplified user summary to the frontend, not the
 * full UserProfile. After fetching from DB, you need to transform the data into
 * a display-friendly format.
 *
 * PROBLEM:
 * Implement `fetchUserSummary(String userId)` that:
 *   1. Fetches the UserProfile asynchronously (use supplyAsync)
 *   2. Transforms it into a UserSummary (use thenApply)
 *   3. Returns CompletableFuture<UserSummary>
 *
 * UserSummary format:
 *   - displayName: first part of email (before @), e.g., "alice@company.com" → "alice"
 *   - badge: based on tier — "Premium" → "⭐", "Enterprise" → "🏢", else "👤"
 *
 * WHAT YOU NEED TO FIGURE OUT:
 *   - How do you chain a transformation on a CompletableFuture result?
 *   - What's the difference between thenApply and thenAccept?
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class E02_ThenApply {

    private static final UserDB db = new UserDB();
    record UserSummary(String displayName, String badge) {}

    public static CompletableFuture<UserSummary> fetchUserSummary(String userId){
        return CompletableFuture.
                supplyAsync(()->db.getUserById(userId))
                .thenApply(userProfile -> {
                    String displayName = userProfile.email().split("@")[0];
                    String badge = userProfile.tier();
                    return new UserSummary(displayName,badge);
                });
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Fetching user summaries...\n");

        // TODO: Uncomment once you implement fetchUserSummary
         CompletableFuture<UserSummary> aliceFuture = fetchUserSummary("U-1001");
         CompletableFuture<UserSummary> carolFuture = fetchUserSummary("U-1003");

         System.out.println("Requests sent, doing other work...");

         UserSummary alice = aliceFuture.get();
         UserSummary carol = carolFuture.get();

         System.out.println("Alice: " + alice);  // Expected: UserSummary[displayName=alice, badge=⭐]
         System.out.println("Carol: " + carol);  // Expected: UserSummary[displayName=carol, badge=🏢]
    }
}

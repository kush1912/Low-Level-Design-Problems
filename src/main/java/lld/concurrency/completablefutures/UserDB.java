package lld.concurrency.completablefutures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.ToString;

import java.io.InputStream;
import java.util.*;

/**
 * Simulates a database with network latency.
 * Data loaded from db_data.json — treat this like PostgreSQL/MySQL.
 */
@ToString
public class UserDB {

    private static final int NETWORK_LATENCY_MS = 200;
    private final Map<String, UserProfile> users = new HashMap<>();
    private final Map<String, List<Order>> ordersByUser = new HashMap<>();
    private final Map<String, List<Notification>> notificationsByUser = new HashMap<>();

    public UserDB() {
        loadData();
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUBLIC API — These simulate real DB queries with network latency
    // ─────────────────────────────────────────────────────────────────────

    public UserProfile getUserById(String userId) {
        simulateLatency();
        return users.get(userId);
    }

    public List<Order> getOrdersByUserId(String userId) {
        simulateLatency();
        return ordersByUser.getOrDefault(userId, List.of());
    }

    public List<Notification> getUnreadNotifications(String userId) {
        simulateLatency();
        return notificationsByUser.getOrDefault(userId, List.of())
                .stream()
                .filter(n -> !n.read())
                .toList();
    }

    public List<UserProfile> getAllUsers() {
        simulateLatency();
        return new ArrayList<>(users.values());
    }

    // ─────────────────────────────────────────────────────────────────────
    // DOMAIN OBJECTS
    // ─────────────────────────────────────────────────────────────────────

    public record UserProfile(String userId, String email, String tier, int loginCount) {}
    public record Order(String orderId, String userId, double amount, String status) {}
    public record Notification(String notifId, String userId, String message, boolean read) {}

    // ─────────────────────────────────────────────────────────────────────
    // INTERNALS
    // ─────────────────────────────────────────────────────────────────────

    private void simulateLatency() {
        try {
            Thread.sleep(NETWORK_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void loadData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getClassLoader().getResourceAsStream("db_data.json");
            if (is == null) {
                throw new RuntimeException("db_data.json not found in classpath");
            }
            JsonNode root = mapper.readTree(is);

            for (JsonNode u : root.get("users")) {
                UserProfile profile = new UserProfile(
                    u.get("userId").asText(),
                    u.get("email").asText(),
                    u.get("tier").asText(),
                    u.get("loginCount").asInt()
                );
                users.put(profile.userId(), profile);
            }

            for (JsonNode o : root.get("orders")) {
                Order order = new Order(
                    o.get("orderId").asText(),
                    o.get("userId").asText(),
                    o.get("amount").asDouble(),
                    o.get("status").asText()
                );
                ordersByUser.computeIfAbsent(order.userId(), k -> new ArrayList<>()).add(order);
            }

            for (JsonNode n : root.get("notifications")) {
                Notification notif = new Notification(
                    n.get("notifId").asText(),
                    n.get("userId").asText(),
                    n.get("message").asText(),
                    n.get("read").asBoolean()
                );
                notificationsByUser.computeIfAbsent(notif.userId(), k -> new ArrayList<>()).add(notif);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load db_data.json", e);
        }
    }
}

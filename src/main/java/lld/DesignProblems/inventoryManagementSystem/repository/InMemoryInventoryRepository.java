package lld.DesignProblems.inventoryManagementSystem.repository;

import lld.DesignProblems.inventoryManagementSystem.model.InventoryItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory data layer for the demo, backed by a thread-safe map so
 * concurrent requests (e.g. two stock adjustments at once) don't corrupt
 * state. Swappable for a real database repository behind the same
 * {@link InventoryRepository} contract.
 */
@Repository
public class InMemoryInventoryRepository implements InventoryRepository {

    private final Map<String, InventoryItem> store = new ConcurrentHashMap<>();

    @Override
    public InventoryItem save(InventoryItem item) {
        store.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<InventoryItem> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<InventoryItem> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public boolean existsBySku(String sku) {
        return store.values().stream().anyMatch(item -> item.getSku().equalsIgnoreCase(sku));
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }
}

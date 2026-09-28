package lld.DesignProblems.inventoryManagementSystem.repository;

import lld.DesignProblems.inventoryManagementSystem.model.InventoryItem;

import java.util.List;
import java.util.Optional;

/**
 * Data-layer contract for inventory items. Kept as an interface so the
 * in-memory implementation could be swapped for a JPA/Mongo-backed one
 * without touching the service layer.
 */
public interface InventoryRepository {

    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findById(String id);

    List<InventoryItem> findAll();

    boolean existsBySku(String sku);

    void deleteById(String id);

    boolean existsById(String id);
}

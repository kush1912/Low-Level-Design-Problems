package lld.DesignProblems.inventoryManagementSystem.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Core domain entity for the Inventory Management System.
 * <p>
 * This is a plain in-memory data-layer object (no JPA/ORM annotations) since
 * the demo persists data in a {@code ConcurrentHashMap}. Status is always
 * kept in sync with quantity via {@link #recalculateStatus()} so callers can
 * never set an inconsistent state directly.
 */
@Getter
@Setter
public class InventoryItem {

    private String id;
    private String sku;
    private String name;
    private String description;
    private Category category;
    private BigDecimal unitPrice;
    private int quantity;
    private int reorderThreshold;
    private ItemStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public InventoryItem() {
    }

    public InventoryItem(String id, String sku, String name, String description, Category category,
                          BigDecimal unitPrice, int quantity, int reorderThreshold) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.reorderThreshold = reorderThreshold;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        recalculateStatus();
    }

    /**
     * Recomputes {@link #status} from the current {@link #quantity} and
     * {@link #reorderThreshold}. Must be called after any stock mutation.
     */
    public void recalculateStatus() {
        if (this.quantity <= 0) {
            this.status = ItemStatus.OUT_OF_STOCK;
        } else if (this.quantity <= this.reorderThreshold) {
            this.status = ItemStatus.LOW_STOCK;
        } else {
            this.status = ItemStatus.IN_STOCK;
        }
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}

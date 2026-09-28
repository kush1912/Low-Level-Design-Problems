package lld.DesignProblems.inventoryManagementSystem.dto;

import lld.DesignProblems.inventoryManagementSystem.model.Category;
import lld.DesignProblems.inventoryManagementSystem.model.InventoryItem;
import lld.DesignProblems.inventoryManagementSystem.model.ItemStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Outward-facing representation of an {@link InventoryItem}. Kept separate
 * from the entity so the wire format can evolve independently of the
 * persisted/data-layer shape.
 */
@Getter
@Setter
public class ItemResponse {

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

    public static ItemResponse from(InventoryItem item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setSku(item.getSku());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setCategory(item.getCategory());
        response.setUnitPrice(item.getUnitPrice());
        response.setQuantity(item.getQuantity());
        response.setReorderThreshold(item.getReorderThreshold());
        response.setStatus(item.getStatus());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}

package lld.DesignProblems.inventoryManagementSystem.dto;

import lld.DesignProblems.inventoryManagementSystem.model.Category;
import lld.DesignProblems.inventoryManagementSystem.model.ItemStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Unified search/filter criteria used by:
 * <ul>
 *   <li>{@code GET /api/v1/inventory-items} - built from individual
 *       {@code @RequestParam}s (single category/status, simple search).</li>
 *   <li>{@code QUERY /api/v1/inventory-items/query} - deserialized directly
 *       from the JSON request body, which lets a client express richer,
 *       multi-value filters (several categories/statuses at once) that don't
 *       fit cleanly in a query string.</li>
 * </ul>
 */
@Getter
@Setter
public class ItemQueryRequest {

    private List<Category> categories;
    private List<ItemStatus> statuses;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String search;

    private int page = 0;
    private int size = 10;
    private String sortBy = "name";
    private String sortDir = "asc";
}

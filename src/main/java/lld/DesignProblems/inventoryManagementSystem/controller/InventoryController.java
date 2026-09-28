package lld.DesignProblems.inventoryManagementSystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lld.DesignProblems.inventoryManagementSystem.dto.CreateItemRequest;
import lld.DesignProblems.inventoryManagementSystem.dto.ItemQueryRequest;
import lld.DesignProblems.inventoryManagementSystem.dto.ItemResponse;
import lld.DesignProblems.inventoryManagementSystem.dto.PagedResponse;
import lld.DesignProblems.inventoryManagementSystem.dto.StockAdjustmentRequest;
import lld.DesignProblems.inventoryManagementSystem.dto.UpdateItemRequest;
import lld.DesignProblems.inventoryManagementSystem.model.Category;
import lld.DesignProblems.inventoryManagementSystem.model.ItemStatus;
import lld.DesignProblems.inventoryManagementSystem.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST API for the Inventory Management System.
 * <p>
 * Exercises the full verb set expected of a modern REST resource:
 * <ul>
 *   <li>{@code POST}   - create an item</li>
 *   <li>{@code GET}    - fetch one item, or list/filter/sort/paginate all items</li>
 *   <li>{@code PUT}    - full replace of an item</li>
 *   <li>{@code PATCH}  - partial update (stock adjustment only)</li>
 *   <li>{@code DELETE} - remove an item</li>
 *   <li>{@code QUERY}  - RFC 10008's new "safe search with a body" method, for
 *       complex multi-value filters that don't fit well in a query string</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/inventory-items")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody CreateItemRequest request,
                                                    UriComponentsBuilder uriComponentsBuilder) {
        ItemResponse created = inventoryService.createItem(request);
        URI location = uriComponentsBuilder.path("/api/v1/inventory-items/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable String id) {
        return ResponseEntity.ok(inventoryService.getItem(id));
    }

    /**
     * Filter + sort + paginate via plain query parameters, e.g.:
     * {@code GET /api/v1/inventory-items?category=ELECTRONICS&minPrice=10&maxPrice=500
     * &search=phone&page=0&size=5&sortBy=price&sortDir=desc}
     */
    @GetMapping
    public ResponseEntity<PagedResponse<ItemResponse>> listItems(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        ItemQueryRequest criteria = new ItemQueryRequest();
        criteria.setCategories(category == null ? null : List.of(category));
        criteria.setStatuses(status == null ? null : List.of(status));
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setSearch(search);
        criteria.setPage(page);
        criteria.setSize(size);
        criteria.setSortBy(sortBy);
        criteria.setSortDir(sortDir);

        return ResponseEntity.ok(inventoryService.searchItems(criteria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable String id,
                                                    @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(inventoryService.updateItem(id, request));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ItemResponse> adjustStock(@PathVariable String id,
                                                     @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Handles the HTTP {@code QUERY} method (RFC 10008): a safe, cacheable,
     * read-only method - like GET - but one that carries a request body -
     * like POST - so clients can express rich, structured search criteria
     * (e.g. several categories/statuses at once) that a query string
     * struggles with.
     * <p>
     * Spring Framework (as of the 6.2.x line used here) does not yet expose
     * {@code RequestMethod.QUERY} on {@code @RequestMapping}
     * (tracked in spring-projects/spring-framework#36988), but its servlet
     * dispatcher still forwards unrecognized HTTP methods into the normal
     * handler-mapping flow. We therefore map this path without restricting
     * the {@code method} attribute and verify the actual verb manually,
     * rejecting anything other than {@code QUERY} with a
     * {@code 405 Method Not Allowed}.
     */
    @RequestMapping(value = "/query")
    public ResponseEntity<PagedResponse<ItemResponse>> queryItems(@Valid @RequestBody ItemQueryRequest request,
                                                                   HttpServletRequest servletRequest) {
        if (!"QUERY".equalsIgnoreCase(servletRequest.getMethod())) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                    "This endpoint only supports the HTTP QUERY method (RFC 10008); received " + servletRequest.getMethod());
        }
        return ResponseEntity.ok(inventoryService.searchItems(request));
    }
}

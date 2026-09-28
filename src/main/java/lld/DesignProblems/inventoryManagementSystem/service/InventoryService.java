package lld.DesignProblems.inventoryManagementSystem.service;

import lld.DesignProblems.inventoryManagementSystem.dto.CreateItemRequest;
import lld.DesignProblems.inventoryManagementSystem.dto.ItemQueryRequest;
import lld.DesignProblems.inventoryManagementSystem.dto.ItemResponse;
import lld.DesignProblems.inventoryManagementSystem.dto.PagedResponse;
import lld.DesignProblems.inventoryManagementSystem.dto.StockAdjustmentRequest;
import lld.DesignProblems.inventoryManagementSystem.dto.UpdateItemRequest;

/**
 * Business/service layer for inventory items. Controllers depend on this
 * interface (not the repository) so validation, id generation and business
 * rules (duplicate SKU checks, stock-status recalculation) live in one place.
 */
public interface InventoryService {

    ItemResponse createItem(CreateItemRequest request);

    ItemResponse getItem(String id);

    ItemResponse updateItem(String id, UpdateItemRequest request);

    ItemResponse adjustStock(String id, StockAdjustmentRequest request);

    void deleteItem(String id);

    PagedResponse<ItemResponse> searchItems(ItemQueryRequest criteria);
}

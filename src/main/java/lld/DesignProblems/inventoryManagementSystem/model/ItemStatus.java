package lld.DesignProblems.inventoryManagementSystem.model;

/**
 * Stock status of an item. Derived automatically from {@code quantity} and
 * {@code reorderThreshold} whenever stock changes, rather than being set
 * directly by clients.
 */
public enum ItemStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}

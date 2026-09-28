package lld.DesignProblems.inventoryManagementSystem.exception;

/**
 * Thrown when an inventory item cannot be found by id.
 * Translated to a {@code 404 Not Found} by {@link GlobalExceptionHandler}.
 */
public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(String id) {
        super("Inventory item not found with id: " + id);
    }
}

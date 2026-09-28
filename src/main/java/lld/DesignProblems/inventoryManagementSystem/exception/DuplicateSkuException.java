package lld.DesignProblems.inventoryManagementSystem.exception;

/**
 * Thrown when a client tries to create an item with a SKU that already
 * exists. Translated to a {@code 409 Conflict} by {@link GlobalExceptionHandler}.
 */
public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) {
        super("An item with sku '" + sku + "' already exists");
    }
}

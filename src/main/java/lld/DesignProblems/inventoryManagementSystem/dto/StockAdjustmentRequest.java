package lld.DesignProblems.inventoryManagementSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for {@code PATCH /api/v1/inventory-items/{id}/stock}.
 * A positive {@code delta} restocks the item, a negative one consumes stock.
 */
@Getter
@Setter
public class StockAdjustmentRequest {

    @NotNull(message = "delta is required")
    private Integer delta;

    private String reason;
}

package lld.DesignProblems.inventoryManagementSystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lld.DesignProblems.inventoryManagementSystem.model.Category;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /api/v1/inventory-items}.
 */
@Getter
@Setter
public class CreateItemRequest {

    @NotBlank(message = "sku is required")
    private String sku;

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    @NotNull(message = "category is required")
    private Category category;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "unitPrice must be greater than 0")
    private BigDecimal unitPrice;

    @Min(value = 0, message = "quantity cannot be negative")
    private int quantity;

    @Min(value = 0, message = "reorderThreshold cannot be negative")
    private int reorderThreshold;
}

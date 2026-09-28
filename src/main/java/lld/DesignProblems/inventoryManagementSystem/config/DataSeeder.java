package lld.DesignProblems.inventoryManagementSystem.config;

import lld.DesignProblems.inventoryManagementSystem.dto.CreateItemRequest;
import lld.DesignProblems.inventoryManagementSystem.model.Category;
import lld.DesignProblems.inventoryManagementSystem.service.InventoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds a handful of sample inventory items on startup so the API is
 * immediately explorable without first calling POST by hand.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final InventoryService inventoryService;

    public DataSeeder(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void run(String... args) {
        seed("SKU-1001", "Wireless Mouse", "Ergonomic 2.4GHz wireless mouse", Category.ELECTRONICS, "19.99", 120, 20);
        seed("SKU-1002", "Mechanical Keyboard", "Hot-swappable RGB mechanical keyboard", Category.ELECTRONICS, "89.50", 8, 10);
        seed("SKU-1003", "Basmati Rice 5kg", "Premium long-grain basmati rice", Category.GROCERY, "12.25", 0, 15);
        seed("SKU-1004", "Cotton T-Shirt", "Unisex crew-neck cotton t-shirt", Category.APPAREL, "9.99", 300, 50);
        seed("SKU-1005", "Office Chair", "Adjustable ergonomic office chair", Category.FURNITURE, "149.00", 15, 5);
        seed("SKU-1006", "Building Blocks Set", "250-piece creative building blocks", Category.TOYS, "24.99", 40, 10);
    }

    private void seed(String sku, String name, String description, Category category, String price, int quantity, int reorderThreshold) {
        CreateItemRequest request = new CreateItemRequest();
        request.setSku(sku);
        request.setName(name);
        request.setDescription(description);
        request.setCategory(category);
        request.setUnitPrice(new BigDecimal(price));
        request.setQuantity(quantity);
        request.setReorderThreshold(reorderThreshold);
        inventoryService.createItem(request);
    }
}

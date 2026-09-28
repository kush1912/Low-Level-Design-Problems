package lld.DesignProblems.inventoryManagementSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Standalone Spring Boot entry point for the Inventory Management System
 * demo. Component-scanning is scoped to this package (and its
 * sub-packages: controller/service/repository/model/dto/exception/middleware/config)
 * so it runs in isolation from the other (non-Spring) design-problem
 * exercises in this repository.
 * <p>
 * Run with: {@code ./gradlew bootRun}
 * (see the {@code springBoot.mainClass} override in build.gradle)
 */
@SpringBootApplication
public class InventoryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryManagementApplication.class, args);
    }
}

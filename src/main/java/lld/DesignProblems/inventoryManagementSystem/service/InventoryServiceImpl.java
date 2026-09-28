package lld.DesignProblems.inventoryManagementSystem.service;

import lld.DesignProblems.inventoryManagementSystem.dto.CreateItemRequest;
import lld.DesignProblems.inventoryManagementSystem.dto.ItemQueryRequest;
import lld.DesignProblems.inventoryManagementSystem.dto.ItemResponse;
import lld.DesignProblems.inventoryManagementSystem.dto.PagedResponse;
import lld.DesignProblems.inventoryManagementSystem.dto.StockAdjustmentRequest;
import lld.DesignProblems.inventoryManagementSystem.dto.UpdateItemRequest;
import lld.DesignProblems.inventoryManagementSystem.exception.DuplicateSkuException;
import lld.DesignProblems.inventoryManagementSystem.exception.ItemNotFoundException;
import lld.DesignProblems.inventoryManagementSystem.model.InventoryItem;
import lld.DesignProblems.inventoryManagementSystem.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;

    public InventoryServiceImpl(InventoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public ItemResponse createItem(CreateItemRequest request) {
        if (repository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }
        InventoryItem item = new InventoryItem(
                UUID.randomUUID().toString(),
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getCategory(),
                request.getUnitPrice(),
                request.getQuantity(),
                request.getReorderThreshold());
        return ItemResponse.from(repository.save(item));
    }

    @Override
    public ItemResponse getItem(String id) {
        return ItemResponse.from(findOrThrow(id));
    }

    @Override
    public ItemResponse updateItem(String id, UpdateItemRequest request) {
        InventoryItem item = findOrThrow(id);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setUnitPrice(request.getUnitPrice());
        item.setQuantity(request.getQuantity());
        item.setReorderThreshold(request.getReorderThreshold());
        item.recalculateStatus();
        item.touch();
        return ItemResponse.from(repository.save(item));
    }

    @Override
    public ItemResponse adjustStock(String id, StockAdjustmentRequest request) {
        InventoryItem item = findOrThrow(id);
        int newQuantity = item.getQuantity() + request.getDelta();
        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                    "Cannot reduce quantity below 0 (current=" + item.getQuantity() + ", delta=" + request.getDelta() + ")");
        }
        item.setQuantity(newQuantity);
        item.recalculateStatus();
        item.touch();
        return ItemResponse.from(repository.save(item));
    }

    @Override
    public void deleteItem(String id) {
        if (!repository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Override
    public PagedResponse<ItemResponse> searchItems(ItemQueryRequest criteria) {
        List<InventoryItem> filtered = repository.findAll().stream()
                .filter(item -> criteria.getCategories() == null || criteria.getCategories().isEmpty()
                        || criteria.getCategories().contains(item.getCategory()))
                .filter(item -> criteria.getStatuses() == null || criteria.getStatuses().isEmpty()
                        || criteria.getStatuses().contains(item.getStatus()))
                .filter(item -> criteria.getMinPrice() == null
                        || item.getUnitPrice().compareTo(criteria.getMinPrice()) >= 0)
                .filter(item -> criteria.getMaxPrice() == null
                        || item.getUnitPrice().compareTo(criteria.getMaxPrice()) <= 0)
                .filter(item -> criteria.getSearch() == null || criteria.getSearch().isBlank()
                        || item.getName().toLowerCase(Locale.ROOT).contains(criteria.getSearch().toLowerCase(Locale.ROOT))
                        || item.getSku().toLowerCase(Locale.ROOT).contains(criteria.getSearch().toLowerCase(Locale.ROOT)))
                .sorted(resolveComparator(criteria.getSortBy(), criteria.getSortDir()))
                .collect(Collectors.toList());

        int page = Math.max(criteria.getPage(), 0);
        int size = criteria.getSize() <= 0 ? 10 : criteria.getSize();
        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<ItemResponse> pageContent = filtered.subList(fromIndex, toIndex).stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());

        return new PagedResponse<>(pageContent, page, size, filtered.size(), criteria.getSortBy(), criteria.getSortDir());
    }

    private Comparator<InventoryItem> resolveComparator(String sortBy, String sortDir) {
        Comparator<InventoryItem> comparator = switch (sortBy == null ? "name" : sortBy.toLowerCase(Locale.ROOT)) {
            case "price", "unitprice" -> Comparator.comparing(InventoryItem::getUnitPrice, Comparator.nullsLast(BigDecimal::compareTo));
            case "quantity" -> Comparator.comparingInt(InventoryItem::getQuantity);
            case "createdat" -> Comparator.comparing(InventoryItem::getCreatedAt);
            case "status" -> Comparator.comparing(item -> item.getStatus() == null ? "" : item.getStatus().name());
            default -> Comparator.comparing(item -> item.getName().toLowerCase(Locale.ROOT));
        };
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private InventoryItem findOrThrow(String id) {
        return repository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }
}

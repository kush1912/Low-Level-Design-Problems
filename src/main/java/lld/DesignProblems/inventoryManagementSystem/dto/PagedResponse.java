package lld.DesignProblems.inventoryManagementSystem.dto;

import lombok.Getter;

import java.util.List;

/**
 * Generic paginated envelope returned by both the filtering GET endpoint and
 * the QUERY endpoint, so clients get a consistent shape regardless of which
 * HTTP method they used to search.
 */
@Getter
public class PagedResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final String sortBy;
    private final String sortDir;

    public PagedResponse(List<T> content, int page, int size, long totalElements, String sortBy, String sortDir) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        this.sortBy = sortBy;
        this.sortDir = sortDir;
    }
}

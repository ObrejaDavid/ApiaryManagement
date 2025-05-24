package org.apiary.utils.pagination;

/**
 * Represents pagination information for a query
 */
public class Pageable {
    private final int page;
    private final int size;
    private final String sortBy;
    private final String sortDirection;

    /**
     * Create a new pageable with default sorting (by id, ascending)
     * @param page The page number (0-based)
     * @param size The page size
     */
    public Pageable(int page, int size) {
        this(page, size, "id", "asc");
    }

    /**
     * Create a new pageable with custom sorting
     * @param page The page number (0-based)
     * @param size The page size
     * @param sortBy The field to sort by
     * @param sortDirection The sort direction ("asc" or "desc")
     */
    public Pageable(int page, int size, String sortBy, String sortDirection) {
        this.page = Math.max(0, page); // Ensure page is non-negative
        this.size = Math.max(1, size); // Ensure size is at least 1
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    /**
     * Get the page number (0-based)
     * @return The page number
     */
    public int getPage() {
        return page;
    }

    /**
     * Get the page size
     * @return The page size
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the field to sort by
     * @return The field to sort by
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * Get the sort direction
     * @return The sort direction ("asc" or "desc")
     */
    public String getSortDirection() {
        return sortDirection;
    }

    /**
     * Check if sorting is ascending
     * @return true if sorting is ascending, false otherwise
     */
    public boolean isSortAscending() {
        return "asc".equalsIgnoreCase(sortDirection);
    }

    /**
     * Get the offset (number of items to skip)
     * @return The offset
     */
    public int getOffset() {
        return page * size;
    }

    /**
     * Create a new pageable for the next page
     * @return A new pageable for the next page
     */
    public Pageable next() {
        return new Pageable(page + 1, size, sortBy, sortDirection);
    }

    /**
     * Create a new pageable for the previous page
     * @return A new pageable for the previous page, or the first page if there is no previous page
     */
    public Pageable previous() {
        return new Pageable(Math.max(0, page - 1), size, sortBy, sortDirection);
    }

    /**
     * Create a new pageable for the first page
     * @return A new pageable for the first page
     */
    public Pageable first() {
        return new Pageable(0, size, sortBy, sortDirection);
    }

    /**
     * Create a new pageable with a different page size
     * @param newSize The new page size
     * @return A new pageable with the specified page size
     */
    public Pageable withSize(int newSize) {
        return new Pageable(page, newSize, sortBy, sortDirection);
    }

    /**
     * Create a new pageable with different sorting
     * @param newSortBy The new field to sort by
     * @param newSortDirection The new sort direction
     * @return A new pageable with the specified sorting
     */
    public Pageable withSort(String newSortBy, String newSortDirection) {
        return new Pageable(page, size, newSortBy, newSortDirection);
    }
}
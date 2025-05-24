package org.apiary.utils.pagination;

import java.util.List;

/**
 * Represents a page of items
 * @param <T> The type of items in the page
 */
public class Page<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;

    /**
     * Create a new page
     * @param content The content of the page
     * @param pageNumber The page number (0-based)
     * @param pageSize The page size
     * @param totalElements The total number of elements
     */
    public Page(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
    }

    /**
     * Get the content of the page
     * @return The content of the page
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * Get the page number (0-based)
     * @return The page number
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Get the page size
     * @return The page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Get the total number of elements
     * @return The total number of elements
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * Get the total number of pages
     * @return The total number of pages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Check if this is the first page
     * @return true if this is the first page, false otherwise
     */
    public boolean isFirst() {
        return pageNumber == 0;
    }

    /**
     * Check if this is the last page
     * @return true if this is the last page, false otherwise
     */
    public boolean isLast() {
        return pageNumber == totalPages - 1;
    }

    /**
     * Check if there is a next page
     * @return true if there is a next page, false otherwise
     */
    public boolean hasNext() {
        return pageNumber < totalPages - 1;
    }

    /**
     * Check if there is a previous page
     * @return true if there is a previous page, false otherwise
     */
    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    /**
     * Get the page number of the next page
     * @return The page number of the next page, or the last page if there is no next page
     */
    public int getNextPageNumber() {
        return hasNext() ? pageNumber + 1 : pageNumber;
    }

    /**
     * Get the page number of the previous page
     * @return The page number of the previous page, or the first page if there is no previous page
     */
    public int getPreviousPageNumber() {
        return hasPrevious() ? pageNumber - 1 : 0;
    }
}
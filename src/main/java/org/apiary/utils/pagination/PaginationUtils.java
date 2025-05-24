package org.apiary.utils.pagination;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for pagination
 */
public class PaginationUtils {

    // Prevent instantiation
    private PaginationUtils() {
    }

    /**
     * Create a page of items from a list
     * @param items The list of items
     * @param pageable The pagination information
     * @param <T> The type of items
     * @return A page of items
     */
    public static <T> Page<T> createPage(List<T> items, Pageable pageable) {
        int totalItems = items.size();
        int fromIndex = pageable.getOffset();
        int toIndex = Math.min(fromIndex + pageable.getSize(), totalItems);

        List<T> pageContent;
        if (fromIndex < totalItems) {
            pageContent = items.subList(fromIndex, toIndex);
        } else {
            pageContent = new ArrayList<>();
        }

        return new Page<>(pageContent, pageable.getPage(), pageable.getSize(), totalItems);
    }

    /**
     * Calculate the total number of pages for a given number of items and page size
     * @param totalItems The total number of items
     * @param pageSize The page size
     * @return The total number of pages
     */
    public static int calculateTotalPages(long totalItems, int pageSize) {
        return pageSize > 0 ? (int) Math.ceil((double) totalItems / pageSize) : 0;
    }

    /**
     * Calculate the offset for a given page number and page size
     * @param pageNumber The page number (0-based)
     * @param pageSize The page size
     * @return The offset
     */
    public static int calculateOffset(int pageNumber, int pageSize) {
        return pageNumber * pageSize;
    }

    /**
     * Generate a list of page numbers to display in pagination controls
     * @param currentPage The current page number (0-based)
     * @param totalPages The total number of pages
     * @param maxPageLinks The maximum number of page links to display
     * @return A list of page numbers to display
     */
    public static List<Integer> generatePageNumbers(int currentPage, int totalPages, int maxPageLinks) {
        List<Integer> pageNumbers = new ArrayList<>();

        if (totalPages <= maxPageLinks) {
            // If there are fewer pages than the maximum number of links, show all pages
            for (int i = 0; i < totalPages; i++) {
                pageNumbers.add(i);
            }
        } else {
            // Show a window of pages around the current page
            int half = maxPageLinks / 2;
            int start = Math.max(0, currentPage - half);
            int end = Math.min(totalPages - 1, start + maxPageLinks - 1);

            // Adjust the start if we're near the end
            if (end - start < maxPageLinks - 1) {
                start = Math.max(0, end - maxPageLinks + 1);
            }

            for (int i = start; i <= end; i++) {
                pageNumbers.add(i);
            }
        }

        return pageNumbers;
    }
}
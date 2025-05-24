package org.apiary.utils;

/**
 * Utility methods for working with strings
 */
public class StringUtils {

    // Prevent instantiation
    private StringUtils() {
    }

    /**
     * Check if a string is null or empty
     * @param str The string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Check if a string is null, empty, or contains only whitespace
     * @param str The string to check
     * @return true if the string is null, empty, or contains only whitespace, false otherwise
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Get a string value, or a default value if the string is null or empty
     * @param str The string to check
     * @param defaultValue The default value to return if the string is null or empty
     * @return The string value, or the default value if the string is null or empty
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * Get a string value, or a default value if the string is null, empty, or contains only whitespace
     * @param str The string to check
     * @param defaultValue The default value to return if the string is null, empty, or contains only whitespace
     * @return The string value, or the default value if the string is null, empty, or contains only whitespace
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * Capitalize the first letter of a string
     * @param str The string to capitalize
     * @return The capitalized string, or null if the input is null
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Truncate a string to a maximum length, appending an ellipsis if necessary
     * @param str The string to truncate
     * @param maxLength The maximum length of the string
     * @return The truncated string, or null if the input is null
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * Check if a string contains another string, ignoring case
     * @param str The string to check
     * @param searchStr The string to search for
     * @return true if the string contains the search string (ignoring case), false otherwise
     */
    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.toLowerCase().contains(searchStr.toLowerCase());
    }
}
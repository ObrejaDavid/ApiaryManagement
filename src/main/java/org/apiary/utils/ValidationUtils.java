package org.apiary.utils;

import java.util.regex.Pattern;

/**
 * Utility methods for validating input data
 */
public class ValidationUtils {

    // Regular expressions for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9\\s()-]{10,20}$");

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_-]{3,20}$");

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    // Prevent instantiation
    private ValidationUtils() {
    }

    /**
     * Validate an email address
     * @param email The email address to validate
     * @return true if the email address is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate a phone number
     * @param phone The phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate a username
     * @param username The username to validate
     * @return true if the username is valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate a password
     * @param password The password to validate
     * @return true if the password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate a numeric value
     * @param value The value to validate
     * @return true if the value is numeric, false otherwise
     */
    public static boolean isNumeric(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate a positive numeric value
     * @param value The value to validate
     * @return true if the value is a positive number, false otherwise
     */
    public static boolean isPositiveNumeric(String value) {
        if (!isNumeric(value)) {
            return false;
        }
        return Double.parseDouble(value) > 0;
    }

    /**
     * Validate a non-negative numeric value
     * @param value The value to validate
     * @return true if the value is a non-negative number, false otherwise
     */
    public static boolean isNonNegativeNumeric(String value) {
        if (!isNumeric(value)) {
            return false;
        }
        return Double.parseDouble(value) >= 0;
    }

    /**
     * Validate an integer value
     * @param value The value to validate
     * @return true if the value is an integer, false otherwise
     */
    public static boolean isInteger(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate a positive integer value
     * @param value The value to validate
     * @return true if the value is a positive integer, false otherwise
     */
    public static boolean isPositiveInteger(String value) {
        if (!isInteger(value)) {
            return false;
        }
        return Integer.parseInt(value) > 0;
    }

    /**
     * Validate a non-negative integer value
     * @param value The value to validate
     * @return true if the value is a non-negative integer, false otherwise
     */
    public static boolean isNonNegativeInteger(String value) {
        if (!isInteger(value)) {
            return false;
        }
        return Integer.parseInt(value) >= 0;
    }
}
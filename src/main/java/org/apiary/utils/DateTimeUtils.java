package org.apiary.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utility methods for working with dates and times
 */
public class DateTimeUtils {

    // Common date and time formats
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    public static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");

    // Prevent instantiation
    private DateTimeUtils() {
    }

    /**
     * Format a local date to a string using the default date formatter
     * @param date The date to format
     * @return The formatted date string, or null if the date is null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format a local time to a string using the default time formatter
     * @param time The time to format
     * @return The formatted time string, or null if the time is null
     */
    public static String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(TIME_FORMATTER);
    }

    /**
     * Format a local date time to a string using the default date time formatter
     * @param dateTime The date time to format
     * @return The formatted date time string, or null if the date time is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Format a local date to a display string
     * @param date The date to format
     * @return The formatted date string, or null if the date is null
     */
    public static String formatDateForDisplay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    /**
     * Format a local time to a display string
     * @param time The time to format
     * @return The formatted time string, or null if the time is null
     */
    public static String formatTimeForDisplay(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(DISPLAY_TIME_FORMATTER);
    }

    /**
     * Format a local date time to a display string
     * @param dateTime The date time to format
     * @return The formatted date time string, or null if the date time is null
     */
    public static String formatDateTimeForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DISPLAY_DATE_TIME_FORMATTER);
    }

    /**
     * Parse a string to a local date using the default date formatter
     * @param dateStr The date string to parse
     * @return The parsed local date, or null if the string is invalid
     */
    public static LocalDate parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parse a string to a local time using the default time formatter
     * @param timeStr The time string to parse
     * @return The parsed local time, or null if the string is invalid
     */
    public static LocalTime parseTime(String timeStr) {
        if (StringUtils.isBlank(timeStr)) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parse a string to a local date time using the default date time formatter
     * @param dateTimeStr The date time string to parse
     * @return The parsed local date time, or null if the string is invalid
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (StringUtils.isBlank(dateTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Convert a java.util.Date to a LocalDateTime
     * @param date The date to convert
     * @return The converted LocalDateTime, or null if the date is null
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Convert a LocalDateTime to a java.util.Date
     * @param dateTime The LocalDateTime to convert
     * @return The converted Date, or null if the LocalDateTime is null
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Calculate the difference between two dates in days
     * @param startDate The start date
     * @param endDate The end date
     * @return The difference in days
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculate the difference between two date times in hours
     * @param startDateTime The start date time
     * @param endDateTime The end date time
     * @return The difference in hours
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * Calculate the difference between two date times in minutes
     * @param startDateTime The start date time
     * @param endDateTime The end date time
     * @return The difference in minutes
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    /**
     * Get the current date
     * @return The current date
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * Get the current time
     * @return The current time
     */
    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }

    /**
     * Get the current date time
     * @return The current date time
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
}
package com.quizsystem.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Formats timestamps for the console. */
public final class DateUtil {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtil() {
    }

    /** Formats a date and a time, for example {@code 2026-11-02 18:26:21}. */
    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME);
    }

    /** Formats only the date part, for example {@code 2026-11-02}. */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE);
    }
}

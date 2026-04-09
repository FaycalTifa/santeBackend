package com.uab.sante.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String DATETIME_PATTERN = "dd/MM/yyyy HH:mm:ss";

    public static String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern(DATETIME_PATTERN));
    }

    public static LocalDate parseDate(String date) {
        if (date == null || date.isEmpty()) return null;
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_PATTERN));
    }
}

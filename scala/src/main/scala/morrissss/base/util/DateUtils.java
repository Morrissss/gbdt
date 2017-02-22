package morrissss.base.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH");
    public static DateTimeFormatter PARTITION_DTM_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public static DateTimeFormatter PARTITION_DTMH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH");
    public static DateTimeFormatter DTM_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static DateTimeFormatter DTMH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    public static String currentTimeStr() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    public static String currentDateStr() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }

    public static String timeStr(LocalDateTime time) {
        return time.format(TIME_FORMATTER);
    }

    public static String dateStr(LocalDateTime time) {
        return time.format(DATE_FORMATTER);
    }

    public static String dtmStr(LocalDateTime time) {
        return time.format(DTM_FORMATTER);
    }

    public static String dtmhStr(LocalDateTime time) {
        return time.format(DTMH_FORMATTER);
    }

    public static String hourStr(LocalDateTime time) {
        return time.format(HOUR_FORMATTER);
    }

    public static String partitionStr(LocalDateTime time) {
        return time.format(PARTITION_DTMH_FORMATTER);
    }

    public static int secondsOfDays(int day) {
        return day * 24 * 60 * 60;
    }

    public static int secondsOfHours(int hour) {
        return hour * 60 * 60;
    }
}

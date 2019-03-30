package com.alibaba.otter.shared.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateUtils {


    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String TIMESTAMP_PATTERN_SECOND = "yyyy-MM-dd HH:mm:ss";

    public static final DateTimeFormatter MILLI_SECOND_FORMATER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);
    public static final DateTimeFormatter SECOND_FORMATER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN_SECOND);


    public static String nowStr() {
        return MILLI_SECOND_FORMATER.format(LocalDateTime.now(ZoneOffset.of("+8")));
    }

    public static String nowStr(Long second) {
        return SECOND_FORMATER.format(LocalDateTime.ofEpochSecond(nowSecond() + second, 0, ZoneOffset.of("+8")));
    }


    public static int nowYear() {
        return LocalDateTime.now(ZoneOffset.of("+8")).getYear();
    }


    public static Long nowSecond() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
    }

    public static Long nowMilliSecond() {
        return LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    public static LocalDateTime now() {
        return LocalDateTime.ofEpochSecond(nowSecond(), 0, ZoneOffset.of("+8"));
    }

    public static Long getNowStart() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        return LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0).toEpochSecond(ZoneOffset.of("+8"));
    }

    public static Long getNowEnd() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        return LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59).toEpochSecond(ZoneOffset.of("+8"));
    }

    /**
     * 2018-12-04 10:50:09.000
     *
     * @param args
     */
    public static void main(String[] args) {
        LocalDateTime.ofEpochSecond(nowSecond() - 10 * 60, 0, ZoneOffset.of("+8"));
        System.out.println(getNowStart());
        System.out.println(nowStr(-10*60L));
    }

}

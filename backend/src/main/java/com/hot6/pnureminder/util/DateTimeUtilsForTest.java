package com.hot6.pnureminder.util;

import com.hot6.pnureminder.entity.Lecture;

import java.sql.Time;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class DateTimeUtilsForTest {

    private static LocalDateTime localDateTestTime = LocalDateTime.of(2023, 4, 24, 14, 40);


    public static ZonedDateTime getCurrentSeoulTime() {
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        ZonedDateTime seoulZonedDateTime = localDateTestTime.atZone(seoulZoneId);
        return seoulZonedDateTime;
    }

    public static ZonedDateTime getTempTime() {
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        return Time.valueOf("23:59:00").toLocalTime().atDate(localDateTestTime.toLocalDate()).atZone(seoulZoneId);
    }

    public static ZonedDateTime getLectureStartTime(Lecture lecture) {
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        return lecture.getStartTime().toLocalTime().atDate(localDateTestTime.toLocalDate()).atZone(seoulZoneId);
    }

    public static ZonedDateTime getLectureEndTime(Lecture lecture) {
        ZonedDateTime lectureStartTime = getLectureStartTime(lecture);
        int hours = lecture.getRunTime().toLocalTime().getHour();
        int minutes = lecture.getRunTime().toLocalTime().getMinute();
        return lectureStartTime.plus(hours, ChronoUnit.HOURS).plus(minutes, ChronoUnit.MINUTES);
    }

    public static int getCurrentDayOfWeekAsInt() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY:
                return 0;
            case TUESDAY:
                return 1;
            case WEDNESDAY:
                return 2;
            case THURSDAY:
                return 3;
            case FRIDAY:
                return 4;
            case SATURDAY:
                return 5;
            case SUNDAY:
                return 6;
            default:
                throw new RuntimeException("Unknown day of week: " + dayOfWeek);
        }
    }
}

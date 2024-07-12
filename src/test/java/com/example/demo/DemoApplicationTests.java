package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DemoApplicationTests {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    DateTimeFormatter time_formatter = DateTimeFormatter.ofPattern("HH:mm");
    LocalTime WORKING_START_TIME = LocalTime.parse("09:00", time_formatter);
    LocalTime WORKING_END_TIME = LocalTime.parse("18:00", time_formatter);
    LocalTime DINNER_START_TIME = LocalTime.parse("13:00", time_formatter);
    LocalTime DINNER_END_TIME = LocalTime.parse("14:00", time_formatter);

    double totalSpentTime = 0;

    long solve(String startDateTime, String endDateTime) {
        long startOfCounting = System.nanoTime();
        long result = 0L;
        LocalDate startDate, endDate;
        try {
            startDate = LocalDate.parse(startDateTime, formatter);
            endDate = LocalDate.parse(endDateTime, formatter);
        } catch (Exception e) {
            return -1L;
        }
        endDate = endDate.minusDays(1);
        if (startDate.isBefore(endDate)) {
            long fullDaysCnt = ChronoUnit.DAYS.between(startDate, endDate);
            LocalDate tmpDate = startDate.plusDays(1);
            while (!tmpDate.isAfter(endDate)) {
                if (tmpDate.getDayOfWeek() == DayOfWeek.SATURDAY || tmpDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    --fullDaysCnt;
                }
                tmpDate = tmpDate.plusDays(1);
            }
            result += fullDaysCnt * 8 * 60;
        }
        endDate = endDate.plusDays(1);

        LocalTime startTime, endTime;
        try {
            startTime = LocalTime.parse(startDateTime, formatter);
            endTime = LocalTime.parse(endDateTime, formatter);
        } catch (Exception e) {
            return -1L;
        }
        if (startDate.isBefore(endDate)) {
            if (startDate.getDayOfWeek() != DayOfWeek.SATURDAY && startDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                startTime = startTime.isAfter(WORKING_START_TIME) ? startTime : WORKING_START_TIME;
                if (startTime.isAfter(DINNER_START_TIME)) {
                    startTime = startTime.isAfter(DINNER_END_TIME) ? startTime : DINNER_END_TIME;
                } else {
                    result += Duration.between(startTime, DINNER_START_TIME).toMinutes();
                    startTime = DINNER_END_TIME;
                }
                if (startTime.isBefore(WORKING_END_TIME)) {
                    result += Duration.between(startTime, WORKING_END_TIME).toMinutes();
                }
            }
            if (endDate.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    endDate.getDayOfWeek() != DayOfWeek.SUNDAY) {

                endTime = endTime.isBefore(WORKING_END_TIME) ? endTime : WORKING_END_TIME;
                if (endTime.isBefore(DINNER_END_TIME)) {
                    endTime = endTime.isBefore(DINNER_START_TIME) ? endTime : DINNER_START_TIME;
                } else {
                    result += Duration.between(DINNER_END_TIME, endTime).toMinutes();
                    endTime = DINNER_START_TIME;
                }
                if (endTime.isAfter(WORKING_START_TIME)) {
                    result += Duration.between(WORKING_START_TIME, endTime).toMinutes();
                }
            }
        } else if (startDate.isEqual(endDate)) {
            if (startDate.getDayOfWeek() != DayOfWeek.SATURDAY && startDate.getDayOfWeek() != DayOfWeek.SUNDAY &&
                    startTime.isBefore(endTime) && !startTime.isAfter(WORKING_END_TIME) && !endTime.isBefore(WORKING_START_TIME)) {

                startTime = startTime.isAfter(WORKING_START_TIME) ? startTime : WORKING_START_TIME;
                endTime = endTime.isBefore(WORKING_END_TIME) ? endTime : WORKING_END_TIME;
                if (startTime.isAfter(DINNER_START_TIME)) {
                    startTime = startTime.isAfter(DINNER_END_TIME) ? startTime : DINNER_END_TIME;
                }
                if (endTime.isBefore(DINNER_END_TIME)) {
                    endTime = endTime.isBefore(DINNER_START_TIME) ? endTime : DINNER_START_TIME;
                }
                if (startTime.isBefore(endTime)) {
                    result += Duration.between(startTime, endTime).toMinutes() - 60;
                    if (!endTime.isAfter(DINNER_START_TIME) || !startTime.isBefore(DINNER_END_TIME)) {
                        result += 60;
                    }
                }
            }
        }
        totalSpentTime += (System.nanoTime() - startOfCounting) / 1_000_000_000.0;
        return result;
    }

    @Test
    @Order(1)
    @DisplayName("1. test both rough data")
    void testBothRoughData() {
        String startTime = "";
        String endTime = "11.07/2024 10.10";

        long actual = solve(startTime, endTime);
        long expected = -1L;

        assertEquals(expected, actual, "Didn't match for both rough data");
    }

    @Test
    @Order(2)
    @DisplayName("2. test one rough data")
    void testOneRoughData() {
        String startTime = "11.07.2024 15:30";
        String endTime = "";

        long actual = solve(startTime, endTime);
        long expected = -1L;

        assertEquals(expected, actual, "Didn't match for one rough data");
    }

    @Test
    @Order(3)
    @DisplayName("3. test logically incorrect input")
    void testLogicallyIncorrect() {
        String startTime = "11.07.2024 15:30";
        String endTime = "11.07.2024 10:10";

        long actual = solve(startTime, endTime);
        long expected = 0L;

        assertEquals(expected, actual, "Didn't match for logically incorrect input");
    }

    @Test
    @Order(4)
    @DisplayName("4. test outside of working hours")
    void testOutsideOfWork() {
        String startTime = "10.07.2024 21:00";
        String endTime = "11.07.2024 06:00";

        long actual = solve(startTime, endTime);
        long expected = 0L;

        assertEquals(expected, actual, "Didn't match for outside of working hours");
    }

    @Test
    @Order(5)
    @DisplayName("5. test left overlap with no lunch")
    void testOverlapLeft() {
        String startTime = "11.07.2024 08:45";
        String endTime = "11.07.2024 09:45";

        long actual = solve(startTime, endTime);
        long expected = 45L;

        assertEquals(expected, actual, "Didn't match for left overlap with no lunch");
    }

    @Test
    @Order(6)
    @DisplayName("6. test right overlap with no lunch")
    void testOverlapRight() {
        String startTime = "11.07.2024 16:16";
        String endTime = "11.07.2024 18:45";

        long actual = solve(startTime, endTime);
        long expected = 104L;

        assertEquals(expected, actual, "Didn't match for right overlap with no lunch");
    }

    @Test
    @Order(7)
    @DisplayName("7. test full left interior with no lunch")
    void testFullLeftInterior() {
        String startTime = "11.07.2024 09:00";
        String endTime = "11.07.2024 13:00";

        long actual = solve(startTime, endTime);
        long expected = 240L;

        assertEquals(expected, actual, "Didn't match for full left interior with no lunch");
    }

    @Test
    @Order(8)
    @DisplayName("8. test full right interior with no lunch")
    void testFullRightInterior() {
        String startTime = "11.07.2024 14:00";
        String endTime = "11.07.2024 18:00";

        long actual = solve(startTime, endTime);
        long expected = 240L;

        assertEquals(expected, actual, "Didn't match for full right interior with no lunch");
    }

    @Test
    @Order(9)
    @DisplayName("9. test full covering interval with lunch")
    void testFullCoveringLunch() {
        String startTime = "11.07.2024 01:00";
        String endTime = "12.07.2024 07:00";

        long actual = solve(startTime, endTime);
        long expected = 480L;

        assertEquals(expected, actual, "Didn't match for full covering interval with lunch");
    }

    @Test
    @Order(10)
    @DisplayName("10. test on Saturday")
    void testSaturday() {
        String startTime = "13.07.2024 00:00";
        String endTime = "13.07.2024 23:59";

        long actual = solve(startTime, endTime);
        long expected = 0L;

        assertEquals(expected, actual, "Didn't match for Saturday");
    }
}

package com.example.demo;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemoApplicationTests {

    private final static String INVALID_INPUT_MESSAGE = "Invalid input provided";

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final LocalTime START_WORK_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_WORK_TIME = LocalTime.of(18, 0);
    private static final LocalTime LUNCH_START = LocalTime.of(13, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(14, 0);

    long solve(String startDateTime, String endDateTime) {

        LocalDateTime start, end;

        try {
            start = LocalDateTime.parse(startDateTime, formatter);
            end = LocalDateTime.parse(endDateTime, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid input provided");
        }

        if (end.isBefore(start)) {
            return 0;
        }

        long totalMinutes = 0;

        for (LocalDateTime current = start.withHour(0).withMinute(0).withSecond(0);
             !current.isAfter(end);
             current = current.plusDays(1)) {

            if (isWorkingDay(current)) {
                LocalDateTime dayStart = LocalDateTime.of(current.toLocalDate(), START_WORK_TIME);
                LocalDateTime dayEnd = LocalDateTime.of(current.toLocalDate(), END_WORK_TIME);

                if (current.toLocalDate().isEqual(start.toLocalDate())) {
                    dayStart = dayStart.isBefore(start) ? start : dayStart;
                }
                if (current.toLocalDate().isEqual(end.toLocalDate())) {
                    dayEnd = dayEnd.isAfter(end) ? end : dayEnd;
                }

                if (dayStart.toLocalTime().isAfter(END_WORK_TIME) || dayEnd.toLocalTime().isBefore(START_WORK_TIME)) {
                    continue;
                }

                if (dayStart.toLocalTime().isBefore(START_WORK_TIME)) {
                    dayStart = LocalDateTime.of(current.toLocalDate(), START_WORK_TIME);
                }
                if (dayEnd.toLocalTime().isAfter(END_WORK_TIME)) {
                    dayEnd = LocalDateTime.of(current.toLocalDate(), END_WORK_TIME);
                }

                totalMinutes += calculateMinutesForDay(dayStart, dayEnd);
            }
        }

        return totalMinutes;
    }

    private static boolean isWorkingDay(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    private static long calculateMinutesForDay(LocalDateTime start, LocalDateTime end) {
        long workingMinutes = 0;

        if (start.toLocalTime().isBefore(LUNCH_START)) {
            LocalDateTime lunchStart = LocalDateTime.of(start.toLocalDate(), LUNCH_START);
            LocalDateTime periodEnd = end.isBefore(lunchStart) ? end : lunchStart;
            workingMinutes += Duration.between(start, periodEnd).toMinutes();
        }

        if (end.toLocalTime().isAfter(LUNCH_END)) {
            LocalDateTime lunchEnd = LocalDateTime.of(start.toLocalDate(), LUNCH_END);
            LocalDateTime periodStart = start.isAfter(lunchEnd) ? start : lunchEnd;
            workingMinutes += Duration.between(periodStart, end).toMinutes();
        }

        return workingMinutes;
    }

    @Test
    @Order(1)
    @DisplayName("1. test both rough data")
    void testBothRoughData() {
        String startTime = "";
        String endTime = "11.07/2024 10.10";

        String info = "StartTime: " + startTime + "\n" + "EndTime: " + endTime + "\n";

        DemoApplicationTests service = new DemoApplicationTests();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.solve(startTime, endTime));

        assertEquals(INVALID_INPUT_MESSAGE, exception.getMessage(), info);
    }

    @Test
    @Order(2)
    @DisplayName("2. test one rough data")
    void testOneRoughData() {
        String startTime = "11.07.2024 15:30";
        String endTime = "";

        String info = "StartTime: " + startTime + "\n" + "EndTime: " + endTime + "\n";

        DemoApplicationTests service = new DemoApplicationTests();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.solve(startTime, endTime));
        assertEquals(INVALID_INPUT_MESSAGE, exception.getMessage(), info);
    }

    @Test
    @Order(3)
    @DisplayName("3. test logically incorrect input")
    void testLogicallyIncorrect() {
        String startTime = "11.07.2024 15:30";
        String endTime = "11.07.2024 10:10";

        String info = "StartTime: " + startTime + "\n" + "EndTime: " + endTime + "\n";

        long actual = assertDoesNotThrow(() -> solve(startTime, endTime), INVALID_INPUT_MESSAGE + '\n' + info);
        long expected = 0L;

        assertEquals(expected, actual, "Didn't match for logically incorrect input");
    }

    @Test
    @Order(4)
    @DisplayName("4. test outside of working hours")
    void testOutsideOfWork() {
        String startTime = "10.07.2024 21:00";
        String endTime = "11.07.2024 06:00";

        String info = "StartTime: " + startTime + "\n" + "EndTime: " + endTime + "\n";

        long actual = assertDoesNotThrow(() -> solve(startTime, endTime), INVALID_INPUT_MESSAGE + '\n' + info);
        long expected = 0L;

        assertEquals(expected, actual, "Didn't match for outside of working hours");
    }

    @Test
    @Order(5)
    @DisplayName("5. test left overlap with no lunch")
    void testOverlapLeft() {
        String startTime = "11.07.2024 08:45";
        String endTime = "11.07.2024 09:45";

        String info = "StartTime: " + startTime + "\n" + "EndTime: " + endTime + "\n";

        long actual = assertDoesNotThrow(() -> solve(startTime, endTime), INVALID_INPUT_MESSAGE + '\n' + info);
        long expected = 45L;

        assertEquals(expected, actual, "Didn't match for left overlap with no lunch");
    }

    @Test
    @Order(6)
    @DisplayName("6. test right overlap with no lunch")
    void testOverlapRight() {
        String startTime = "11.07.2024 16:16";
        String endTime = "11.07.2024 18:45";

        String info = "StartTime: " + startTime + "\n" + "EndTime: " + endTime + "\n";

        long actual = assertDoesNotThrow(() -> solve(startTime, endTime), INVALID_INPUT_MESSAGE + '\n' + info);
        long expected = 104L;

        assertEquals(expected, actual, "Didn't match for right overlap with no lunch");
    }

    @Test
    @Order(7)
    @DisplayName("7. test full left internal with no lunch")
    void testFullLeftInterior() {
        String startTime = "11.07.2024 09:00";
        String endTime = "11.07.2024 13:00";

        String info = "StartTime: " + startTime + "\n" + "EndTime: " + endTime + "\n";

        long actual = assertDoesNotThrow(() -> solve(startTime, endTime), INVALID_INPUT_MESSAGE + '\n' + info);
        long expected = 240L;

        assertEquals(expected, actual, "Didn't match for full left internal with no lunch");
    }

    @Test
    @Order(8)
    @DisplayName("8. test full right internal with no lunch")
    void testFullRightInterior() {
        String startTime = "11.07.2024 14:00";
        String endTime = "11.07.2024 18:00";

        String info = "StartTime: " + startTime + "\n" + "EndTime: " + endTime + "\n";

        long actual = assertDoesNotThrow(() -> solve(startTime, endTime), INVALID_INPUT_MESSAGE + '\n' + info);
        long expected = 240L;

        assertEquals(expected, actual, "Didn't match for full right internal with no lunch");
    }

    @Test
    @Order(9)
    @DisplayName("9. test full covering interval with lunch")
    void testFullCoveringLunch() {
        String startTime = "11.07.2024 01:00";
        String endTime = "12.07.2024 07:00";

        String info = "StartTime: " + startTime + "\n" + "EndTime: " + endTime + "\n";

        long actual = assertDoesNotThrow(() -> solve(startTime, endTime), INVALID_INPUT_MESSAGE + '\n' + info);
        long expected = 480L;

        assertEquals(expected, actual, "Didn't match for full covering interval with lunch");
    }

    @Test
    @Order(10)
    @DisplayName("10. test working on weekend")
    void testSaturday() {
        String startTime = "13.07.2024 00:00";
        String endTime = "14.07.2024 23:59";

        String info = "StartTime: " + startTime + "\n" + "EndTime: " + endTime + "\n";

        long actual = assertDoesNotThrow(() -> solve(startTime, endTime), INVALID_INPUT_MESSAGE + '\n' + info);
        long expected = 0L;

        assertEquals(expected, actual, "Didn't match for Saturday");
    }
}

package com.example.demo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemoApplicationTests {

    private final static String INVALID_INPUT_MESSAGE = "Invalid input provided";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final DateTimeFormatter time_formatter = DateTimeFormatter.ofPattern("HH:mm");
    private final LocalTime WORKING_START_TIME = LocalTime.parse("09:00", time_formatter);
    private final LocalTime WORKING_END_TIME = LocalTime.parse("18:00", time_formatter);
    private final LocalTime DINNER_START_TIME = LocalTime.parse("13:00", time_formatter);
    private final LocalTime DINNER_END_TIME = LocalTime.parse("14:00", time_formatter);

    long solve(String startDateTime, String endDateTime) {

        long result = 0L;
        LocalDate startDate, endDate;

        try {
            startDate = LocalDate.parse(startDateTime, formatter);
            endDate = LocalDate.parse(endDateTime, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid input provided");
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
            throw new IllegalArgumentException("Invalid input provided");
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

        return result;
    }

    private static boolean isWorkingDay(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    @ParameterizedTest
    @Order(1)
    @DisplayName("1. test both rough data")
    @CsvSource(value = {", 11.07/2024 10.10"})
    void testBothRoughData(String startTime, String endTime) {

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

    @ParameterizedTest
    @Order(3)
    @DisplayName("3. test logically incorrect input")
    @CsvSource(value = {"11.07.2024 15:30, 11.07.2024 10:10"})
    void testLogicallyIncorrect(String startTime, String endTime) {

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

    @ParameterizedTest
    @Order(5)
    @DisplayName("5. test left overlap with no lunch")
    @CsvSource(value = {"11.07.2024 08:45, 11.07.2024 09:45"})
    void testOverlapLeft(String startTime, String endTime) {

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

    @ParameterizedTest
    @Order(7)
    @DisplayName("7. test full left internal with no lunch")
    @CsvSource(value = {"11.07.2024 09:00, 11.07.2024 13:00"})
    void testFullLeftInterior(String startTime, String endTime) {

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

    @ParameterizedTest
    @Order(9)
    @DisplayName("9. test full covering interval with lunch")
    @CsvSource(value = {"11.07.2024 01:00, 12.07.2024 07:00"})
    void testFullCoveringLunch(String startTime, String endTime) {

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

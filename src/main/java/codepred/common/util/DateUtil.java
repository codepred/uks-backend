package codepred.common.util;

import codepred.payment.dto.ValueDto;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DateUtil {

    public static List<LocalDateTime> generateTimesForDateTimeString(String dateTimeString) {
        List<LocalDateTime> dateTimeList = new ArrayList<>();

        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString);
        LocalDateTime date = zonedDateTime.toLocalDateTime();
        ZoneOffset zoneOffset = zonedDateTime.getOffset();

        for (int hour = 0; hour <= 23; hour++) {
            for (int minute = 0; minute <= 45; minute += 15) {
                LocalDateTime dateTime = date
                    .withHour(hour)
                    .withMinute(minute)
                    .withSecond(0)
                    .withNano(0);

                dateTime = dateTime.atOffset(zoneOffset).toLocalDateTime();

                dateTimeList.add(dateTime);
            }
        }

        return dateTimeList;
    }

    public static class DateContainer {
        public LocalDateTime startDate;
        public LocalDateTime endDate;

        public DateContainer(LocalDateTime startDate, LocalDateTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    public static List<ValueDto> convertToLocalTimeList(List<LocalDateTime> dateTimeList) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<ValueDto> timeStrings = dateTimeList.stream()
            .map(dateTime -> new ValueDto(dateTime.format(formatter)))
            .collect(Collectors.toList());

        return timeStrings;
    }

    public static String convertToEndOfTheDay(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime timeZero = LocalTime.of(23, 59);
            date.atTime(timeZero).withSecond(0).withNano(0);
            String formattedDate = date.atTime(timeZero).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

            return formattedDate;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String convertToStartOfTheDay(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime timeZero = LocalTime.of(0, 0);
            date.atTime(timeZero).withSecond(0).withNano(0);
            String formattedDate = date.atTime(timeZero).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            return formattedDate;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static LocalDateTime convertToDateTime(String dateTimeString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
            return localDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String extractDate(String dateTimeString) {
        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(dateTimeString);
            String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return formattedDate;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long getMinutes(LocalDateTime startTime) {
        Instant codeTimeGenerated = startTime.atZone(ZoneId.of("Europe/Warsaw")).toInstant();
        Instant now = Instant.now();

        Duration duration = Duration.between(codeTimeGenerated, now);
        if(duration.toMinutes() < 0){
            return duration.toMinutes() * -1;
        }
        return -9000000000L;
    }

    public static final LocalDateTime parseTimeFromString(String timeString, DayOfWeek dayOfWeek) {
        String[] timeParts = timeString.split(":");
        if (timeParts.length != 2) {
            // Invalid time format, return null or handle the error as needed
            return null;
        }

        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        return LocalDateTime.now().with(DayOfWeek.from(dayOfWeek)).withHour(hour).withMinute(minute);
    }
}

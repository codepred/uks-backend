package codepred.common.mapper;

import codepred.calendar.availability.NewAvailabilityRequest;
import codepred.calendar.get.EventDataDto;
import codepred.calendar.get.EventFullDataDto;
import codepred.calendar.save.NewEventRequest;
import codepred.calendar.update.UpdateEventDto;
import codepred.meeting.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ClientMapper.class})
public interface EventMapper {

    EventDataDto toDto(Event event);

    Event fromEventDTO(NewEventRequest NewEventRequest);

    Event fromUpdateEventDto(UpdateEventDto UpdateEventDto);

    EventFullDataDto toEventFullDataDto(Event event);

    default LocalDateTime stringToLocalDateTime(String input) {
        String[] parts = input.toLowerCase().split(" ");
        String dayOfWeekStr = parts[0];
        String time = parts[1];

        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        LocalDate referenceDate = LocalDate.now();
        DayOfWeek desiredDay = DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());

        while (!referenceDate.getDayOfWeek().equals(desiredDay)) {
            referenceDate = referenceDate.plusDays(1);
        }

        return LocalDateTime.of(referenceDate, LocalTime.of(hour, minute));
    }

    @Mapping(source = "type", target = "type")
    @Mapping(source = "isAvailability", target = "isAvailability")
    List<Event> dtoToEvent(List<NewAvailabilityRequest> availabilityDTO);


}

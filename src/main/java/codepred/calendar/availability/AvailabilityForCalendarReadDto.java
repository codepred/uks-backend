package codepred.calendar.availability;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class AvailabilityForCalendarReadDto {

    private Integer id;
    private List<Integer> daysOfWeek;
    private String backgroundColor;
    @JsonFormat(pattern = "HH:mm")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalDateTime endTime;
}

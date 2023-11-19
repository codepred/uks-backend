package codepred.calendar.update;

import codepred.meeting.MeetingType;
import codepred.meeting.RepeatMeeting;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class UpdateEventDto {
    @NotEmpty(message = "EVENT_ID_REQUIRED")
    @NotNull(message = "EVENT_ID_REQUIRED")
    @Schema(description = "Event id", example = "20")
    @JsonProperty("eventId")
    private Integer id;
    @Schema(description = "Start Time", example = "2023-01-10T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "End Time", example = "2023-01-10T10:30:00")
    private LocalDateTime endTime;

    private RepeatMeeting repeatEvent;

    private MeetingType type;
    private String title;

    private String additionalInformation;

    private Boolean notifyUsers;

}

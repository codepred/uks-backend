package codepred.calendar.save;

import codepred.meeting.MeetingType;
import codepred.meeting.RepeatMeeting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class NewEventRequest {

    @Schema(description = "Start Time", example = "2023-01-10T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "End Time", example = "2023-01-10T10:30:00")
    private LocalDateTime endTime;

    private String title;

    private RepeatMeeting repeatEvent;

    private MeetingType type;

    private String additionalInformation;

    private Boolean notifyUsers;

    private Integer serviceId;

    private Integer clientId;

    private Boolean isGroup;
}

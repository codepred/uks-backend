package codepred.calendar.get;

import codepred.meeting.RepeatMeeting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class EventFullDataDto {

    @Schema(description = "Event id", example = "5")
    private Integer id;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Start time of event")
    private LocalDateTime startTime;

    @Schema(description = "End time of event")
    private LocalDateTime endTime;

    @Schema(description = "Repeat event", example = "NONE/ EVERY_WEEK / EVERY_MONTH")
    private RepeatMeeting repeatEvent;

    @Schema(description = "MeetingType of event", example = "dostępność/niedostępność")
    private String type;

    @Schema(description = "Additional information", example = "Zajęcia przeznaczone na gramatykę")
    private String additionalInformation;

    private Boolean notifyUsers;

    @Schema(description = "Client data")
    private ClientForEventDto client;

    @Schema(description = "Service data")
    private ServiceEventDto service;
}

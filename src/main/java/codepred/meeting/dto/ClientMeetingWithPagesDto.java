package codepred.meeting.dto;

import codepred.meeting.MeetingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientMeetingWithPagesDto {
    @Schema(description = "id of clients", example = "3")
    private Integer clientId;
    private int pageNumber;
    @Schema(description = "start time")
    private LocalDateTime startTime;
    @Schema(description = "end time")
    private LocalDateTime endTime;
    private String meetingType;
}

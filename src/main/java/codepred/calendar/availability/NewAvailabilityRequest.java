package codepred.calendar.availability;

import codepred.meeting.MeetingType;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class NewAvailabilityRequest {
    @Schema(description = "Start date", example = "friday 8:00")
    private String startTime;
    @Schema(description = "End date", example = "friday 9:00")
    private String endTime;

    @Hidden
    private MeetingType meetingType = MeetingType.AVAILABLE;

    @Hidden
    private Boolean isAvailability = Boolean.TRUE;
}

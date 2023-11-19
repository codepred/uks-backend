package codepred.meeting.dto;

import codepred.meeting.MeetingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
public class MeetingReadDto {
    private Integer eventId;
    @Schema(description = "date")
    private LocalDateTime date;
    @Schema(description = "Group name")
    private String groupName;
    @Schema(description = "Meeting status")
    private MeetingStatus meetingStatus;
    @Schema(description = "price")
    private Double price;

    public MeetingReadDto(Integer eventId, LocalDateTime date, String groupName, MeetingStatus meetingStatus, Double price) {
        this.eventId = eventId;
        this.date = date;
        this.groupName = groupName;
        this.meetingStatus = meetingStatus;
        this.price = price;
    }
}

package codepred.group.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
public class GroupMeetingDto {
    private Integer groupId;
    private Integer pageNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String meetingType;
}

package codepred.group.dto;

import codepred.meeting.MeetingStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
public class GroupMeetingReadDto {
    private Integer id;

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private MeetingStatus meetingStatus;

    private Double price;

    private Integer countPresentStudents;

    private Integer numberOfStudents;


    public GroupMeetingReadDto(Integer id, LocalDateTime date, MeetingStatus meetingStatus, Double price, Integer countPresentStudents, Integer numberOfStudents) {
        this.id = id;
        this.date = date;
        this.meetingStatus = meetingStatus;
        this.price = price;
        this.countPresentStudents = countPresentStudents;
        this.numberOfStudents=numberOfStudents;
    }
}

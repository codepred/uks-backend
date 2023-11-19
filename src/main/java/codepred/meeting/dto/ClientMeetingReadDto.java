package codepred.meeting.dto;

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
public class ClientMeetingReadDto {

    private Integer id;

    private String meetingType;

    private String groupName;

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private MeetingStatus meetingStatus;

    private Double price;

    private Float valuePaid;

    private Integer eventId;

    public ClientMeetingReadDto(Integer id, String meetingType, LocalDateTime date, MeetingStatus meetingStatus, Double price, Float valuePaid, Integer eventId) {
        this.id = id;
        this.meetingType = meetingType;
        this.date = date;
        this.meetingStatus = meetingStatus;
        this.price = price;
        this.valuePaid=valuePaid;
        this.eventId=eventId;
    }

    public ClientMeetingReadDto(Integer id,
                                String meetingType,
                                String groupName,
                                LocalDateTime date,
                                MeetingStatus meetingStatus,
                                Double price,
                                Float valuePaid,
                                Integer eventId) {
        this.id = id;
        this.meetingType = meetingType;
        this.groupName = groupName;
        this.date = date;
        this.meetingStatus = meetingStatus;
        this.price = price;
        this.valuePaid = valuePaid;
        this.eventId = eventId;
    }
}

package codepred.meeting.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class MeetingsDto {

    String numberOfPages;
    String summedValue;
    String paidValue;
    List<Integer> meetingIds;
}

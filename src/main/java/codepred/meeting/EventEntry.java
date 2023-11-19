package codepred.meeting;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventEntry {
    private String startTime;
    private String endTime;
    private String type;

}

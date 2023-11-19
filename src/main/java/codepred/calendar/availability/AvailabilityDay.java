package codepred.calendar.availability;

import codepred.meeting.EventEntry;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AvailabilityDay {
    private String translatedName;
    private Boolean isCheckboxSelected=Boolean.TRUE;
    private List<EventEntry> events;
}

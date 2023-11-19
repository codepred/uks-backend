package codepred.calendar.remove;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
//@AllArgsConstructor
@ToString
public class RemoveEventRequest {
    @Schema(description = "Event id", example = "12")
    private String eventId;
    @JsonCreator
    public RemoveEventRequest(@JsonProperty("eventId") String eventId) {
        this.eventId = eventId;
    }
}

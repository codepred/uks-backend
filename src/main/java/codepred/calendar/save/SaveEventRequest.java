package codepred.calendar.save;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class SaveEventRequest {
    @Schema(description = "event data")
    private NewEventRequest requestEventDTO;
}

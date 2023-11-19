package codepred.calendar.save;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientAndEventRequest {
    @Schema(description = "Event id", example = "12")
    private Integer eventId;

    @Schema(description = "Client id", example = "11")
    private Integer clientId;
}

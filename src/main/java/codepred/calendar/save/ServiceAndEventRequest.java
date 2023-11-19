package codepred.calendar.save;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ServiceAndEventRequest {
    @Schema(description = "Event Id", example = "1")
    private Integer eventId;

    @Schema(description = "Service Id", example = "2")
    private Integer serviceId;
}

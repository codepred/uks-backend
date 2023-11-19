package codepred.calendar.save;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class GroupAndEventRequest {
    @Schema(description = "Event id", example = "1")
    private Integer eventId;

    @Schema(description = "Group id", example = "2")
    private Integer groupId;
}

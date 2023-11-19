package codepred.calendar.get;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class GroupEventDto {
    @Schema(description = "Group id", example = "1")
    private Integer id;
    @Schema(description = "Group name", example = "Group 1")
    private String groupName;

}

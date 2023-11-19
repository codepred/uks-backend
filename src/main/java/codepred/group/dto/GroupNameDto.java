package codepred.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class GroupNameDto {

    @Schema(description = "Group name", example = "Group 1")
    private String groupName;

}

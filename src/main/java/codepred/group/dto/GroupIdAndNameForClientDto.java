package codepred.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class GroupIdAndNameForClientDto {
    @Schema(description = "Group id", example = "19")
    private Integer id;

    @Schema(description = "Name of group", example = "group 1")
    private String groupName;
}

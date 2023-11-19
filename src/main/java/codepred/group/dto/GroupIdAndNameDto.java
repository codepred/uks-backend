package codepred.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class GroupIdAndNameDto {
    @Schema(description = "Group id", example = "10")
    private Integer id;
    @Schema(description = "Group name", example = "Pro lingua")
    private String name;
}

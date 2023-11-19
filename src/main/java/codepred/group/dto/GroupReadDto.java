package codepred.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class GroupReadDto {
    @Schema(description = "Id", example = "4")
    private Integer id;
    @Schema(description = "Group name", example = "XCOM B1")
    private String groupName;
    @Schema(description = "Group name", example = "XCOM B1")
    private byte[] groupPhoto;


}

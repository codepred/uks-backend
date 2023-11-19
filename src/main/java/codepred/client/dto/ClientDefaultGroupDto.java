package codepred.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientDefaultGroupDto {
    @Schema(description = "Group unique identifier", example = "1")
    private Integer groupId;
    @Schema(description = "Group name", example = "group 1")
    private String groupName;

    public ClientDefaultGroupDto() {

    }
}

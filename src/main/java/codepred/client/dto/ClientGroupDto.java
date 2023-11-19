package codepred.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientGroupDto {
    @Schema(description = "Client id", example = "10")
    private Integer clientId;

    @Schema(description = "Group id", example = "5")
    private Integer groupId;
}

package codepred.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientServiceDto {
    @Schema(description = "Client id", example = "10")
    private Integer clientId;

    @Schema(description = "Service id", example = "3")
    private Integer serviceId;
}

package codepred.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientDto {
    @Schema(description = "id", example = "1")
    private Integer id;
    @Schema(description = "type of client", example = "STUDENT/GROUP/COMPANY")
    private String type;
    @Schema(description = "name", example = "Pro lingua")
    private String name;
}

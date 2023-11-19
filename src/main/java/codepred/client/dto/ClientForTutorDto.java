package codepred.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClientForTutorDto {

    @Schema(description = "Client id", example = "19")
    private Integer id;

    @Schema(description = "Photo")
    private byte[] clientPhoto;

    @Schema(description = "First name", example = "John")
    private String name;

    @Schema(description = "Last name", example = "Doe")
    private String lastname;

    @Schema(description = "Connected group", example = "Client")
    private String groupName;

    @Schema(description = "Connected company", example = "Pro lingua")
    private String companyName;
}

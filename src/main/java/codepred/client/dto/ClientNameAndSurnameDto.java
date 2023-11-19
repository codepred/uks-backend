package codepred.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientNameAndSurnameDto {

    @Schema(description = "Client's first name", example = "Andrew")
    private String name;
    @Schema(description = "Client's last name", example = "Dombrowski")
    private String surname;

}

package codepred.calendar.get;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientEventDto {
    @Schema(description = "Client Id", example = "12")
    private Integer id;
    @Schema(description = "Client's first name", example = "Andrew")
    private String name;
    @Schema(description = "Client's last name", example = "Dombrowski")
    private String surname;

}
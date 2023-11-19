package codepred.calendar.get;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientForEventDto {

    @Schema(description = "Event id", example = "5")
    private Integer id;

    @Schema(description = "Client name", example = "Andrew")
    private String name;

    @Schema(description = "Client lastname", example = "Dombrowski")
    private String lastname;

    @Schema(description = "")
    private String type;

    public ClientForEventDto() {

    }
}

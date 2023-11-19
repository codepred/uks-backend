package codepred.calendar.get;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientsDataDto {
    @Schema(description = "id", example = "1")
    private Integer id;
    @Schema(description = "type", example = "STUDENT/GROUP")
    private String type;
    @Schema(description = "name")
    private String name;
    @Schema(description = "service id")
    private Integer serviceId;
}

package codepred.calendar.get;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ServiceEventDto {

    @Schema(description = "Id", example = "4")
    private Integer id;

    @Schema(description = "Service name", example = "Consulting")
    private String serviceName;

    @Schema(description = "Service duration in minutes", example = "60")
    private Integer serviceDuration;

    @Schema(description = "Time unit of service duration", example = "minute")
    private String timeUnit;

    @Schema(description = "Price", example = "123.22")
    private Double price;
}

package codepred.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientDefaultServiceDto {
    @Schema(description = "Service's unique identifier", example = "1")
    private Integer serviceId;

    @Schema(description = "Service's name", example = "Standard Cleaning")
    private String serviceName;

    @Schema(description = "Service's duration in minutes", example = "60")
    private Integer serviceDuration;

    @Schema(description = "Unit of time for service", example = "minutes")
    private String timeUnit;

    @Schema(description = "Service's price", example = "50")
    private Double price;

    public ClientDefaultServiceDto() {

    }
}

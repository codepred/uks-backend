package codepred.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ServiceReadDto {
    @Schema(description = "Id", example = "4")
    private Integer id;
    @Schema(description = "Service name", example = "Consulting")
    private String serviceName;

    @Schema(description = "Service duration in minutes", example = "60")
    private Integer serviceDuration;

    @Schema(description = "Service price in cents", example = "100")
    private Double price;

    @Schema(description = "Name for invoice", example = "John Smith")
    private String invoiceName;

    @Schema(description = "PKWiU code", example = "1234")
    private String pkwiu;

    @Schema(description = "Time unit of service duration", example = "minute")
    private String timeUnit;

    @Schema(description = "Value added tax in percent", example = "23")
    private String vat;

    @Schema(description = "Basis for VAT exemption", example = "Something")
    private String basisForVatExemption;
}

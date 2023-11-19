package codepred.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class UpdateServiceRequest {
    @NotNull(message = "SERVICE_ID_NOT_NULL")
    @NotEmpty(message = "SERVICE_ID_NOT_EMPTY")
    @Schema(description = "Service id", example = "1")
    private Integer id;

    @NotNull(message = "SERVICE_NAME_NOT_NULL")
    @NotEmpty(message = "SERVICE_NAME_NOT_EMPTY")
    @Schema(description = "Service name", example = "Consulting")
    private String serviceName;

    @NotNull(message = "SERVICE_DURATION_NOT_NULL")
    @NotEmpty(message = "SERVICE_DURATION_NOT_EMPTY")
    @Schema(description = "Service duration in minutes", example = "60")
    private Integer serviceDuration;

    @NotNull(message = "SERVICE_PRICE_NOT_NULL")
    @NotEmpty(message = "SERVICE_PRICE_NOT_EMPTY")
    @Schema(description = "Service price in cents", example = "100")
    private Double price;

    @NotNull(message = "INVOICE_NAME_NOT_NULL")
    @NotEmpty(message = "INVOICE_NAME_NOT_EMPTY")
    @Schema(description = "Name for invoice", example = "John Smith")
    private String invoiceName;

    @NotNull(message = "PKWIU_NOT_NULL")
    @NotEmpty(message = "PKWIU_NOT_EMPTY")
    @Schema(description = "PKWiU code", example = "1234")
    private String pkwiu;

    @NotNull(message = "TIME_UNIT_NOT_NULL")
    @NotEmpty(message = "TIME_UNIT_NOT_EMPTY")
    @Schema(description = "Time unit of service duration", example = "minute")
    private String timeUnit;

    @NotNull(message = "VAT_NOT_NULL")
    @NotEmpty(message = "VAT_NOT_EMPTY")
    @Schema(description = "Value added tax in percent", example = "23")
    private String vat;

    @Schema(description = "Basis for VAT exemption", example = "Something")
    private String basisForVatExemption;

    @Schema(description = "Set TRUE for first login endpoint", example = "TRUE")
    private Boolean isServiceActive;
}

package codepred.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ClientFullDataDto {

    @Schema(description = "Client's unique identifier", example = "1")
    private Integer id;

    @Schema(description = "Email", example = "example@gmail.com")
    private String email;

    @Schema(description = "Client's name", example = "John")
    private String name;

    @Schema(description = "Client's lastname", example = "Doe")
    private String lastname;

    @Schema(description = "Client's street and number", example = "123 Elm St")
    private String streetAndNumber;

    @Schema(description = "Client's postcode", example = "12345")
    private String postcode;

    @Schema(description = "Client's province", example = "Ontario")
    private String province;

    @Schema(description = "Client's phone number", example = "123-456-7890")
    private String phoneNumber;

    @Schema(description = "Client's color preference", example = "Red")
    private String color;

    @Schema(description = "Client's meeting link", example = "https://meet.google.com/example")
    private String meetingLink;

    @Schema(description = "Client's storage link", example = "https://drive.google.com/example")
    private String storageLink;

    @Schema(description = "Client's minimum change time", example = "30")
    private Integer minimumChangeTime;

    @Schema(description = "Client's additional information", example = "Allergic to peanuts")
    private String additionalInformation;

    @Schema(description = "Client's photo")
    private byte[] photo;

    private String nip;

    private String regon;

    @Schema(description = "If the client has business invoice", example = "true")
    private Boolean isBusinessInvoice;

    @Schema(description = "If the client is available to change reservation", example = "true")
    private Boolean isAvailableToChangeReservation;

    @Schema(description = "If the client is available to cancel reservation", example = "false")
    private Boolean isAvailableToCancelReservation;

    @Schema(description = "Client's default service details")
    private ClientDefaultServiceDto defaultService;

    @Schema(description = "Client's default group")
    private ClientDefaultGroupDto defaultGroupDTO;

    public ClientFullDataDto() {
    }
}

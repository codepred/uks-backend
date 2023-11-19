package codepred.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class NewClientRequest {

    @NotNull(message = "NAME_MUST_BY_NOT_EMPTY")
    @NotEmpty(message = "NAME_MUST_BY_NOT_EMPTY")
    @Schema(description = "Name of an account", example = "John")
    private String name;

    @NotNull(message = "LASTNAME_MUST_BY_NOT_EMPTY")
    @NotEmpty(message = "LASTNAME_MUST_BY_NOT_EMPTY")
    @Schema(description = "Lastname", example = "Doe")
    private String lastname;

    @NotNull(message = "EMAIL_MUST_BY_NOT_EMPTY")
    @NotBlank(message = "EMAIL_MUST_BY_NOT_EMPTY")
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;
    @Schema(description = "Street and number", example = "Jana Pawła 123")
    private String streetAndNumber;

    @Schema(description = "Postal code", example = "12345")
    private String postcode;

    @Schema(description = "Province", example = "Poznan")
    private String province;

    @Schema(description = "Phone number", example = "+1 123-456-7890")
    private String phoneNumber;

    @Schema(description = "Color", example = "Red")
    private String color;

    @Schema(description = "Meeting link", example = "https://example.com/meeting")
    private String meetingLink;

    @Schema(description = "Storage link", example = "https://example.com/storage")
    private String storageLink;

    @Schema(description = "Availability to change reservation", example = "true")
    private Boolean isAvailableToChangeReservation;

    @Schema(description = "Availability to cancel reservation", example = "false")
    private Boolean isAvailableToCancelReservation;

    @Schema(description = "Minimum change time in minutes", example = "60")
    private Integer minimumChangeTime;

    @Schema(description = "Additional information", example = "Lorem ipsum dolor sit amet.")
    private String additionalInformation;

    @Schema(description = "Photo")
    private MultipartFile photo;

    private Boolean changePhoto;
    @Schema(description = "Business invoice", example = "TRUE/FALSE")
    private Boolean isBusinessInvoice;

    private Integer assignedGroup;

    private String nip;

    private String regon;
}

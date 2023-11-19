package codepred.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class CreateCompanyRequest {
    @NotNull(message = "NAME_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "NAME_MUST_NOT_BE_EMPTY")
    @Schema(description = "The name of the company", example = "Pro lingua")
    private String name;

    @NotNull(message = "POSTCODE_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "POSTCODE_MUST_NOT_BE_EMPTY")
    @Schema(description = "The postal code", example = "12345")
    private String postcode;

    @NotNull(message = "ADDRESS_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "ADDRESS_MUST_NOT_BE_EMPTY")
    @Schema(description = "Address", example = "12345")
    private String address;

    @NotNull(message = "PLACE_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "PLACE_MUST_NOT_BE_EMPTY")
    @Schema(description = "The place", example = "Warsaw")
    private String place;

    @NotNull(message = "NIP_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "NIP_MUST_NOT_BE_EMPTY")
    @Schema(description = "The NIP (Tax Identification Number)", example = "1234567890")
    private String nip;

    private String regon;

    @NotNull(message = "ADMIN_NAME_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "ADMIN_NAME_MUST_NOT_BE_EMPTY")
    @Schema(description = "The admin's first name", example = "John")
    private String adminName;

    @NotNull(message = "ADMIN_LASTNAME_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "ADMIN_LASTNAME_MUST_NOT_BE_EMPTY")
    @Schema(description = "The admin's last name", example = "Doe")
    private String adminLastname;

    @NotNull(message = "EMAIL_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "EMAIL_MUST_NOT_BE_EMPTY")
    @Schema(description = "The email address", example = "johndoe@example.com")
    private String email;

    @NotNull(message = "PHONE_NUMBER_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "PHONE_NUMBER_MUST_NOT_BE_EMPTY")
    @Schema(description = "The phone number", example = "1234567890")
    private String phoneNumber;

    @NotNull(message = "AVAILABLE_TO_CHANGE_RESERVATION_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "AVAILABLE_TO_CHANGE_RESERVATION_MUST_NOT_BE_EMPTY")
    @Schema(description = "Indicates whether the account is available to change reservations", example = "true")
    private Boolean isAvailableToChangeReservation;

    @NotNull(message = "AVAILABLE_TO_CANCEL_RESERVATION_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "AVAILABLE_TO_CANCEL_RESERVATION_MUST_NOT_BE_EMPTY")
    @Schema(description = "Indicates whether the account is available to cancel reservations", example = "true")
    private Boolean isAvailableToCancelReservation;

    @NotNull(message = "MINIMUM_CHANGE_TIME_MUST_NOT_BE_EMPTY")
    @NotEmpty(message = "MINIMUM_CHANGE_TIME_MUST_NOT_BE_EMPTY")
    @Schema(description = "The minimum time required for reservation changes in minutes", example = "60")
    private Integer minimumChangeTime;

    @Schema(description = "Additional information", example = "Lorem ipsum dolor sit amet.")
    private String additionalInfo;

    @Schema(description = "Photo")
    private MultipartFile photo;
}

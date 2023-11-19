package codepred.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CompanyFullDataDto {
    @Schema(description = "Id of the company", example = "6")
    private Integer id;

    @Schema(description = "The name of the company", example = "Pro lingua")
    private String name;

    @Schema(description = "Address of the company", example = "Street ...")
    private String address;

    @Schema(description = "Postcode of the company", example = "12-345")
    private String postcode;

    @Schema(description = "Place of the company", example = "Toruń")
    private String place;

    @Schema(description = "NIP", example = "1234567")
    private String nip;

    @Schema(description = "Regon")
    private String regon;

    @Schema(description = "Id of an admin", example = "2")
    private Integer adminId;

    @Schema(description = "email of administrator", example = "email@gmail.com")
    private String email;

    @Schema(description = "Name of administrator", example = "Andrew")
    private String adminName;

    @Schema(description = "Surname of administrator", example = "Dombrowski")
    private String adminSurname;

    @Schema(description = "Surname of administrator", example = "+123456789")
    private String phoneNumber;

    @Schema(description = "Is available to change reservation", example = "+123456789")
    private Boolean isAvailableToChangeReservation;

    @Schema(description = "Is available to cancel reservation", example = "+123456789")
    private Boolean isAvailableToCancelReservation;

    @Schema(description = "Photo")
    private byte[] photo;

    @Schema(description = "Minimum change time", example = "3")
    private Integer minimumChangeTime;

    @Schema(description = "Additional company info", example = "description, notes, books")
    private String additionalInfo;
}

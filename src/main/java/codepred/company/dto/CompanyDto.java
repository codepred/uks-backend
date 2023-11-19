package codepred.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class CompanyDto {

    @Schema(description = "Id of the company", example = "6")
    private Integer id;

    @Schema(description = "The name of the company", example = "Pro lingua")
    private String name;

    @Schema(description = "Number of groups", example = "2")
    private Integer groupnumber;

    @Schema(description = "Number of students", example = "7")
    private Integer studentsnumber;

    @Schema(description = "Photo")
    private byte[] photo;
}

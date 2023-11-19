package codepred.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class CompanyGroupCreateRequest {
    @Schema(description = "Id", example = "4")
    private Integer id;

    @Schema(description = "The name of the company", example = "Pro Lingua")
    private String name;


}

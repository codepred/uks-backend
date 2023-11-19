package codepred.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class ClientReadDto {

    @Schema(description = "Student id", example = "1")
    private Integer id;

    @Schema(description = "name")
    private String name;

    @Schema(description = "lastname")
    private String lastname;
}

package codepred.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GroupsDto {
    @Schema(description = "Id of group", example = "6")
    private Integer id;
    private byte[] groupPhoto;
    @Schema(description = "Group name", example = "XCOM B1")
    private String groupname;
    @Schema(description = "Company name", example = "Pro Lingua")
    private String companyname;
    @Schema(description = "Number of students", example = "3")
    private Integer studentsnumber;
}

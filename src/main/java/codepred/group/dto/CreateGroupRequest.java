package codepred.group.dto;

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
public class CreateGroupRequest {
    @NotNull(message = "GROUP_NAME_NOT_NULL")
    @NotEmpty(message = "GROUP_NAME_NOT_EMPTY")
    @Schema(description = "Group name", example = "Group 1")
    private String groupName;

    @Schema(description = "Color of group", example = "Red")
    private String color;

    @Schema(description = "Link to meeting", example = "Zoom")
    private String linkToMeeting;

    @Schema(description = "Link to folder", example = "Google drive")
    private String folderLink;

    @Schema(description = "Group name", example = "Students characteristic")
    private String additionalInformation;

    @Schema(description = "Photo", example = "...")
    private MultipartFile groupPhoto;
}

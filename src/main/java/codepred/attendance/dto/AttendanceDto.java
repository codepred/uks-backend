package codepred.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class AttendanceDto {

    @Schema(description = "attendance id")
    private Integer attendanceId;

    @Schema(description = "is present")
    private Boolean isPresent;
}

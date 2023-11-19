package codepred.attendance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class GroupAttendancesReadDto {

    private Integer attendanceId;
    private String name;
    private Boolean isPresent;

    public GroupAttendancesReadDto(Integer attendanceId, String name, Boolean isPresent) {
        this.attendanceId = attendanceId;
        this.name = name;
        this.isPresent = isPresent;
    }
}

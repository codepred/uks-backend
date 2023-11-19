package codepred.attendance.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GroupAttendancesDto {

    private List<AttendanceDto> attendanceDto;
}

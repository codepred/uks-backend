package codepred.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
public class CompanyMeetingDto {

    @Schema(description = "company id")
    private Integer companyId;
    @Schema(description = "start time")
    private LocalDateTime startTime;
    @Schema(description = "end time")
    private LocalDateTime endTime;

    public CompanyMeetingDto() {
    }
}

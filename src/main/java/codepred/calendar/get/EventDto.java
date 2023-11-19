package codepred.calendar.get;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventDto {
    @Schema(description = "Event id", example = "20")
    private Integer id;
    @JsonProperty("title")
    private String title;

    @Schema(description = "Start Time", example = "2023-01-10T09:00:00")
    private String backgroundColor;

    @JsonProperty("start")
    @Schema(description = "Start Time", example = "2023-01-10T09:00:00")
    private LocalDateTime startTime;

    @JsonProperty("end")
    @Schema(description = "End Time", example = "2023-01-10T10:30:00")
    private LocalDateTime endTime;
}

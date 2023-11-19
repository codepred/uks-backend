package codepred.calendar.get;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class EventDataDto {

    @Schema(description = "Event id", example = "20")
    private Integer id;

    @JsonProperty("title")
    @Schema(description = "Start Time", example = "2023-01-10T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "End Time", example = "2023-01-10T10:30:00")
    private LocalDateTime endTime;

    @Schema(description = "List of clients", example = "[{\"name\": \"Andrew\", \"surname\": \"Dombrowski\"}, {\"name\": \"Joanna\", \"surname\": \"Michalska\"}]")
    private GroupEventDto groups;
    @Schema(description = "Group name", example = "Group A")
    private ClientEventDto clients;


}
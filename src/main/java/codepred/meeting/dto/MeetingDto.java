package codepred.meeting.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MeetingDto {

    private Integer id;
    private LocalDateTime date;
    private String type;
    private String groupName;
    private String status;
    private Double price;
    private boolean postponePossibility;
    private boolean cancelPossibility;

    public MeetingDto(Integer id, LocalDateTime date, String type, String status, Double price, boolean postponePossibility, boolean cancelPossibility) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.status = status;
        this.price = price;
        this.postponePossibility = postponePossibility;
        this.cancelPossibility = cancelPossibility;
    }
}

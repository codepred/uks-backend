package codepred.tutor.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TutorDto {

    private Integer id;
    private String meetingLink;
    private String storageLink;

    public TutorDto(Integer id, String meetingLink, String storageLink) {
        this.id = id;
        this.meetingLink = meetingLink;
        this.storageLink = storageLink;
    }
}

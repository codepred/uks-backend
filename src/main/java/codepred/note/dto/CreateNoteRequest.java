package codepred.note.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class CreateNoteRequest {
    private Integer clientId;
    private String content;
}

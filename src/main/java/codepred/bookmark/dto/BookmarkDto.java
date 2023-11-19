package codepred.bookmark.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BookmarkDto {
    private Integer id;
    private String displayedName;
    private String fileName;

    public BookmarkDto(Integer id, String displayedName, String fileName) {
        this.id = id;
        this.displayedName = displayedName;
        this.fileName = fileName;
    }
}

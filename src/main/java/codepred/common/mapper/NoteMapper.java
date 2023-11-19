package codepred.common.mapper;

import codepred.note.Note;
import codepred.note.dto.NoteDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NoteMapper {
    List<NoteDto> fromNoteDto(List<Note> notes);
}

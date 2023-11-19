package codepred.note;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {

    @Query(value = "SELECT * FROM notes n " +
            "JOIN users_notes un_tutor ON n.id = un_tutor.notes_id " +
            "JOIN users_notes un_client ON n.id = un_client.notes_id " +
            "WHERE un_tutor.users_id = ?1 AND un_client.users_id = ?2 ORDER BY n.created_at desc", nativeQuery = true)
    List<Note> findByTutorIdAndClientId(Integer tutorId, Integer clientId);

    @Query(value = "select * from notes n " +
            "join groups g on g.id = n.groups_id join users_groups ug on g.id = ug.groups_id where n.groups_id=:groupId and ug.user_id=:tutorId", nativeQuery = true )
    List<Note> findByGroupsAndUsers(@Param("groupId") Integer groupId, @Param("tutorId") Integer tutorId);

    @Modifying
    @Query(value = "delete from users_notes where notes_id=:noteId", nativeQuery = true)
    void removeNoteUserConnection(Integer noteId);

}

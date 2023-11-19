package codepred.group;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
    @Query("SELECT g FROM User u JOIN u.groups g WHERE u.id =:tutorId")
    List<Group> findByUserId(@Param("tutorId") Integer tutorId);

    @Query("SELECT g FROM User u JOIN u.groups g WHERE u.id =:clientId")
    List<Group> findByClientId(@Param("clientId") Integer clientId);

    @Query(value = "select * from groups g join users_groups ug on g.id = ug.groups_id where user_id=:tutorId and company_id=:companyId", nativeQuery = true)
    List<Group> findByClientIdAndCompanyId(@Param("tutorId") Integer tutorId, @Param("companyId") Integer companyId);

    @Query(value = "select * from groups g join users_groups ug on g.id = ug.groups_id where ug.user_id=:tutorId and ug.groups_id=:groupId", nativeQuery = true)
    Optional<Group> findByIdAndTutor(@Param("groupId") Integer groupId, @Param("tutorId") Integer tutorId);


    @Query(value = "select * from groups g join users_groups ug on g.id = ug.groups_id where user_id=:tutorId", nativeQuery = true)
    List<Group> findAllGroup(@Param("tutorId") Integer tutorId);

    @Query(value = "select * from groups g join users_groups ue on g.id = ue.groups_id where ue.groups_id=:groupId and ue.user_id=:tutorId", nativeQuery = true)
    Optional<Group> findGroupByIdAndUser(@Param("groupId") Integer groupId, @Param("tutorId") Integer tutorId);

    @Query(value = "select g.id as groupId, g.color, g.link_to_meeting, g.folder_link, g.group_photo, g.additional_information," +
            " c.id as companyId, c.name as companyName, " +
            " s.id as serviceId, s.price, s.service_name as serviceName, s.service_duration as serviceDuration, s.time_unit as timeUnit" +
            ", g.group_name as group_name " +
            " from groups g " +
            " left join companies c on g.company_id = c.id " +
            " left join services s on g.service_id = s.id join users_groups ug on g.id = ug.groups_id" +
            " where ug.user_id=:tutorId and g.id=:groupId LIMIT 1", nativeQuery = true)
    List<Object[]> findCertainGroup(@Param("groupId") Integer groupId, @Param("tutorId") Integer tutorId);

    @Query(value = "SELECT " +
            " g.id, " +
            " g.group_name AS groupname, " +
            " (select count(*) from users_groups ug where ug.groups_id=g.id and ug.user_id!=:tutorId) AS studentsnumber, " +
            "  c.name AS schoolname," +
            " g.group_photo " +
            "FROM " +
            "  groups g " +
            "JOIN " +
            "  users_groups ug ON g.id = ug.groups_id " +
            "LEFT JOIN " +
            "  companies c ON g.company_id = c.id " +
            "WHERE " +
            "  ug.user_id =:tutorId " +
            "order by groupname",
            countQuery = "select count(*) FROM groups g JOIN users_groups ug ON g.id = ug.groups_id WHERE ug.user_id =:tutorId"
            ,
            nativeQuery = true)
    List<Object[]> getAllGroupConnectedWithTutor(@Param("tutorId") Integer tutorId, Pageable pageable);

    @Query(value = "select * from groups g "
        + "inner join users_groups ug on g.id = ug.groups_id "
        + "where ug.user_id=:tutorId",
            nativeQuery = true)
    List<Group> getAllGroupConnectedWithTutor(@Param("tutorId") Integer tutorId);

    @Query(value = "select * from groups g "
        + "left join users_companies uc on g.company_id = uc.companies_id "
        + "where uc.users_id=:tutorId",
        nativeQuery = true)
    List<Group> getAllGroupConnectedWithCompany(@Param("tutorId") Integer tutorId);

    @Query(value = "SELECT " +
            " g.id, " +
            " g.group_name AS group_name, " +
            "  (select count(*) from users_groups ug where ug.groups_id=g.id and ug.user_id!=:tutorId) AS user_count, " +
            "  c.name AS company_name " +
            "FROM " +
            "  groups g " +
            "JOIN " +
            "  users_groups ug ON g.id = ug.groups_id " +
            "LEFT JOIN " +
            "  companies c ON g.company_id = c.id " +
            "WHERE " +
            "  ug.user_id =:tutorId AND " +
            " lower(g.group_name) like lower(concat('%', :content, '%')) " +
            "order by group_name", nativeQuery = true)
    List<Object[]> findGroupByNameAndTutor(@Param("content") String content, @Param("tutorId") Integer tutorId);

    @Query(value = "SELECT COUNT(*) FROM groups g JOIN users_groups u ON u.groups_id=g.id WHERE u.user_id = :tutorId", nativeQuery = true)
    Integer countGroupByTutor(@Param("tutorId") Integer tutorId);

    @Query(value =
            "SELECT COUNT(DISTINCT ug.user_id) " +
                    "FROM users_groups ug " +
                    "INNER JOIN groups g ON ug.groups_id = g.id " +
                    "WHERE ug.user_id <> :tutorId AND g.id = :groupId " +
                    "AND EXISTS (" +
                    "   SELECT 1 " +
                    "   FROM users_groups tug " +
                    "   WHERE tug.user_id = :tutorId AND tug.groups_id = g.id" +
                    ")",
            nativeQuery = true)
    Integer countStudentInGroup(@Param("tutorId") Integer tutorId, @Param("groupId") Integer groupId);

    @Query(value = "SELECT u.id " +
            "FROM groups g " +
            "JOIN users_groups ug " +
            "ON g.id = ug.groups_id " +
            "JOIN users u ON ug.user_id = u.id " +
            "WHERE g.id = :groupId AND u.id != :tutorId", nativeQuery = true)
    List<Object[]> getConnectedClients(@Param("tutorId") Integer tutorId, @Param("groupId") Integer groupId);


    @Query(value =
        "select id from groups g where company_id=:company_id",
        nativeQuery = true)
    List<Integer> getCompanyGroupsIds(@Param("company_id") Integer company_id);


    @Query(value =
        "select * from groups g inner join users_companies uc on uc.companies_id=g.company_id "
            + "where uc.users_id=:clientId",
        nativeQuery = true)
    List<Group> getAllGroupForAdmin(@Param("clientId") Integer clientId);

    @Modifying
    @Query(value = "delete from users_notes where notes_id=:noteId", nativeQuery = true)
    void removeNotesTutorConnection(@Param("noteId") Integer noteId);

    @Modifying
    @Query(value = "delete from notes where id=:noteId", nativeQuery = true)
    void removeNote(@Param("noteId") Integer noteId);

    @Query(value = "select * from groups g join users_groups ug on g.id = ug.groups_id where ug.groups_id=:groupId and ug.user_id=:tutorId", nativeQuery = true)
    Optional<Group> findGroupByIdAndTutor(@Param("groupId") Integer groupId, @Param("tutorId") Integer tutorId);

    @Query(value = "select * from groups g join events on g.id = events.group_id where events.id=:eventId", nativeQuery = true)
    Optional<Group> findGroupByEvent(@Param("eventId") Integer eventId);

    @Query(value = "select * from groups g " +
        "where g.company_id=:companyId", nativeQuery = true)
    List<Group> getConnectedGroups(@Param("companyId") Integer companyId);

    @Modifying
    @Query(value = "delete from notes where groups_id=:groupId", nativeQuery = true)
    void removeNotesForGroup(@Param("groupId") Integer groupId);

    @Modifying
    @Query(value ="delete from users_groups where groups_id=:groupId", nativeQuery = true)
    void removeStudentsForGroup(@Param("groupId") Integer groupId);

    @Modifying
    @Query(value = "DELETE FROM attendances WHERE event_id IN (SELECT id FROM events WHERE group_id =:groupId);", nativeQuery = true)
    void removeAttendancesForGroup(@Param("groupId") Integer groupId);

    @Modifying
    @Query(value = "delete from events where group_id=:groupId", nativeQuery = true)
    void removeEventsForGroup(@Param("groupId") Integer groupId);

    Optional<Group> findGroupById(Integer id);
}

package codepred.meeting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    @Query("SELECT e FROM Event e JOIN e.users u WHERE u.id = :tutorId and e.isAvailability!=true")
    List<Event> findEventsByUserId(@Param("tutorId") Integer tutorId);

    @Query(value = "SELECT COUNT(e) > 0 FROM Event e JOIN e.users t " +
            "WHERE t.id = :tutorId " +
            "AND ((e.startTime < :endTime AND e.endTime > :startTime) " +
            "OR (e.startTime >= :startTime AND e.startTime < :endTime) " +
            "OR (e.endTime > :startTime AND e.endTime <= :endTime)) " +
            "AND e.isAvailability=false and e.wasCanceled!=true")
    boolean existsByTutorIdAndTimeRange(Integer tutorId, LocalDateTime startTime, LocalDateTime endTime);

    @Query(value = "SELECT e FROM Event e JOIN e.users t " +
            "WHERE t.id = :tutorId " +
            "AND ((e.startTime < :endTime AND e.endTime > :startTime) " +
            "OR (e.startTime >= :startTime AND e.startTime < :endTime) " +
            "OR (e.endTime > :startTime AND e.endTime <= :endTime)) " +
            "AND e.isAvailability=false and e.wasCanceled!=true AND e.id!=:eventId")
    Optional<Event> findEventsAtTimeRangeForTutor(Integer tutorId, Integer eventId, LocalDateTime startTime, LocalDateTime endTime);

    @Query(value = "select * from events e join users_events ue on e.id = ue.events_id where ue.events_id=:eventId and ue.users_id=:tutorId", nativeQuery = true)
    Optional<Event> findEventByIdAndUsers(@Param("eventId") Integer eventId, @Param("tutorId") Integer tutorId);

    @Query(value = "select * from events e join users_events on e.id = users_events.events_id where e.id=:eventId and users_events.users_id=:tutorId and e.is_availability!=true", nativeQuery = true)
    Optional<Event> getEventByIdAndTutor(@Param("eventId") Integer eventId, @Param("tutorId") Integer tutorId);


    @Query(value = "select * from events e join users_events on e.id = users_events.events_id "
        + "where users_events.users_id=:tutorId and start_time>:dateFrom and start_time<:dateTo and e.is_availability=false", nativeQuery = true)
    List<Event> getAllTutorEventByDay(@Param("tutorId") Integer tutorId, @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);

    @Query(value = "SELECT * FROM events JOIN users_events ue ON events.id = ue.events_id " +
            "WHERE ue.users_id = :tutorId AND events.is_availability = true " +
            "AND EXTRACT(DOW FROM events.start_time) = :dayOfWeek",
            nativeQuery = true)
    List<Event> getAvailabilitiesForTutor(@Param("tutorId") Integer tutorId, @Param("dayOfWeek") Integer dayOfWeek);

    @Query(value = "select * from events join users_events ue on events.id = ue.events_id where ue.users_id=:tutorId and events.is_availability=true",
        nativeQuery = true)
    List<Event> getAvailabilitiesForTutor(@Param("tutorId") Integer tutorId);

    @Query(value = "select ue.users_id from events join users_events ue on events.id = ue.events_id where events.id=:eventId and ue.users_id!=:tutorId", nativeQuery = true)
    Integer getClientIdForEvent(@Param("tutorId") Integer tutorId, @Param("eventId") Integer eventId);

    @Query(value = "SELECT cd.name, cd.lastname " +
            "from client_details cd "+
            "where cd.user_id =:clientId " +
            "and cd.tutor_id=:tutorId", nativeQuery = true)
    List<Object[]> getClientDataForEvent(@Param("clientId") Integer clientId, @Param("tutorId") Integer tutorId);

    @Query(value = "SELECT start_time FROM events WHERE is_availability = true AND type = '0'",
            nativeQuery = true)
    List<LocalDateTime> getTutorDaysAvailible(@Param("tutorId") Integer tutorId);

    @Query(value = "select * from events e join users_events ue on e.id = ue.events_id "
            + "where ue.users_id=:tutorId and e.id=:eventId", nativeQuery = true)
    Optional<Event> getDateForAParticularMeeting(@Param("tutorId") Integer tutorId, @Param("eventId") Integer eventId);

    @Modifying
    @Query(value = "delete from users_events where users_id=:tutorId and events_id=:eventId"
            , nativeQuery = true)
    void removeConnectionAvailabilities(@Param("tutorId") Integer tutorId, @Param("eventId") Integer eventId);

    @Modifying
    @Query(value = "delete from events where events.id=:eventId"
            , nativeQuery = true)
    void removeAvailabilities(@Param("eventId") Integer eventId);

    @Modifying
    @Query(value = "update events set service_id=null where id=:eventId", nativeQuery = true)
    void removeServiceInEvent(@Param("eventId") Integer eventId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "update events set service_id=:serviceId where id=:eventId", nativeQuery = true)
    void updateServiceInEvent(@Param("eventId") Integer eventId, @Param("serviceId") Integer serviceId);

    @Modifying
    @Query(value = "delete from users_events where events_id=:eventId and users_id!=:tutorId", nativeQuery = true)
    void removeClientInEvent(@Param("eventId") Integer eventId, @Param("tutorId") Integer tutorId);

    @Modifying
    @Query(value = "update events set group_id=null where id=:eventId", nativeQuery = true)
    void removeGroupInEvent(@Param("eventId") Integer eventId);

    @Query(value = "select * from events e join users_events ue on e.id = ue.events_id where ue.users_id=:tutorId and e.is_availability=true", nativeQuery = true)
    List<Event> getAllAvailabilitiesForTutor(@Param("tutorId") Integer tutorId);

    @Query(value = "select * from events e join users_events ue on e.id = ue.events_id " +
            "where ue.users_id=:tutorId and e.id=:eventId and e.is_availability=false", nativeQuery = true)
    Optional<Event> getCertainEvent(@Param("tutorId") Integer tutorId, @Param("eventId") Integer eventId);

    @Query(value = "select * from events e join users_events ue on e.id = ue.events_id " +
            "where ue.users_id=:tutorId and e.id=:eventId and group_id=:groupId and e.is_availability=false", nativeQuery = true)
    Optional<Event> getGroupEvents(@Param("tutorId") Integer tutorId, @Param("eventId") Integer eventId,Integer groupId);

    @Query(value = "select * from events where group_id=:groupId", nativeQuery = true)
    List<Event> getGroupEvents(@Param("groupId") Integer groupId);

    @Query("select count(*) from Event e " +
            "where e.group.id = ?1 and e.startTime >= ?2 and e.endTime <=?3")
    int countGroupEvents(Integer groupId,LocalDateTime startTime,LocalDateTime endTime);


    @Query(value = "select u.id, u.email from users u  " +
            "join users_groups ug " +
            "ON ug.user_id = u.id " +
            "where ug.user_id!=:tutorId and ug.groups_id=:groupId", nativeQuery = true)
    List<Object []> getConnectedClients(@Param("tutorId") Integer tutorId, @Param("groupId") Integer groupId);

    @Query(value = "select * from events e " +
            "join groups g on e.group_id = g.id " +
            "where g.id=:groupId and start_time >= :startTime and end_time <= :endTime",
            nativeQuery = true)
    Page<Event> findMeetingsByGroupIdAndTimeRange(@Param("groupId") Integer groupId,LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    @Query(value = "select * from events where group_id in (:list) and events.end_time <= :endTime and events.start_time >= :startTime",
        nativeQuery = true)
    Page<Event> findAllEventsForCompanyInTimeRange(@Param("list") List<Integer> list,LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    @Query(value = "select * from events where group_id in (:list) and start_time >= :startTime and end_time <= :endTime",
            nativeQuery = true)
    List<Event> findAllEventsForCompanyInTimeRange(@Param("list") List<Integer> list, LocalDateTime startTime, LocalDateTime endTime);

    @Query(value = "select * from events where group_id in (:list)", nativeQuery = true)
    List<Event> findAllEventsForCompany(@Param("list") List<Integer> list);
}

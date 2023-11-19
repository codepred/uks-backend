package codepred.attendance;

import codepred.meeting.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    @Modifying
    @Query(value = "delete from attendances where event_id=:eventId", nativeQuery = true)
    void removeAttendanceById(Integer eventId);

    @Query(value = "SELECT a.* FROM attendances a " +
            "JOIN public.events e ON e.id = a.event_id " +
            "WHERE a.client_id=:id " +
            "AND e.start_time >= :startTime AND e.end_time <= :endTime " +
            "ORDER BY e.start_time ASC",
            nativeQuery = true)
    Page<Attendance> findByClientIdAndTimeRange(Integer id, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    @Query(value = "SELECT a.* FROM attendances a " +
            "JOIN public.events e ON e.id = a.event_id " +
            "WHERE a.client_id=:id " +
            "AND e.start_time >= :startTime AND e.end_time <= :endTime " +
            "ORDER BY e.start_time DESC",
            nativeQuery = true)
    List<Attendance> findByClientIdAndTimeRange(Integer id, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT a FROM Attendance a " +
            "JOIN a.event e " +
            "WHERE e.group.id = :id " +
            "and a.attendanceStatus!=0 " +
            "AND e.startTime >= :startTime AND e.endTime <= :endTime " +
            "ORDER BY e.startTime DESC")
    List<Attendance> findPresentUserAttendanceByGroupIdAndTimeRange(Integer id, LocalDateTime startTime, LocalDateTime endTime);



    @Query(value = "SELECT * FROM attendances a " +
            "JOIN public.events e ON e.id = a.event_id " +
            "WHERE a.client_id=:id ORDER BY e.start_time DESC",
            nativeQuery = true)
    List<Attendance> findByClientId(Integer id);

    List<Attendance> findAttendancesByEvent(Event event);
}

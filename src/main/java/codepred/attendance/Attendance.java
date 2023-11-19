package codepred.attendance;

import codepred.meeting.Event;
import codepred.meeting.MeetingStatus;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "attendances")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    //"CLIENT_IN_GROUP", "STUDENT"
    private String clientType;

    @Enumerated(EnumType.STRING)
    private MeetingStatus meetingStatus=MeetingStatus.planned;

    private AttendanceStatus attendanceStatus=AttendanceStatus.PRESENT;

    private Integer clientId;

    private Integer serviceId;

    private Float valuePaid;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Event event;

    public Attendance(String clientType, Integer clientId, Integer serviceId, Event event) {
        this.clientType = clientType;
        this.clientId = clientId;
        this.serviceId = serviceId;
        this.event = event;
    }
}

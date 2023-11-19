package codepred.meeting;

import codepred.account.User;
import codepred.attendance.Attendance;
import codepred.group.Group;
import codepred.service.Service;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    // previous vale for start time
    private LocalDateTime oldStartTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String title;

    private String backgroundColor;

    @Enumerated(EnumType.STRING)
    private MeetingStatus meetingStatus=MeetingStatus.planned;

    // dostępność/niedostępność
    private MeetingType type;

    private RepeatMeeting repeatEvent;

    private Boolean isAvailability = Boolean.FALSE;

    private Boolean wasCanceled = Boolean.FALSE;

    @Column(length = 1000)
    private String additionalInformation;

    private Boolean notifyUsers=Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Service service;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "events", fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Set<Attendance> attendances=new HashSet<>();

}

package codepred.group;

import codepred.account.User;
import codepred.company.Company;
import codepred.meeting.Event;
import codepred.note.Note;
import codepred.service.Service;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    private String groupName;

    private String color;

    private String linkToMeeting;

    private String folderLink;

    @Column(length = 1000)
    private String additionalInformation;

    private byte[] groupPhoto;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Event> events = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "groups", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> user = new HashSet<>();

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Company company;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Service service;

    @OneToMany(mappedBy = "groups", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Note> notes = new HashSet<>();

}

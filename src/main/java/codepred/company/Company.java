package codepred.company;

import codepred.account.User;
import codepred.group.Group;
import codepred.service.Service;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "companies")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Company {

    @Id
    @GeneratedValue(strategy= GenerationType.TABLE)
    private Integer id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Unique
    private String name;

    private String address;

    private String postcode;

    private String place;

    private String nip;

    private String regon;

    private Boolean isAvailableToChangeReservation;

    private Boolean isAvailableToCancelReservation;

    private Integer minimumChangeTime;

    @Column(length = 1000)
    private String additionalInfo;

    private byte[] photo;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "companies", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Service> services;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Group> groups;
}

package codepred.service;


import codepred.account.User;
import codepred.group.Group;
import codepred.meeting.Event;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringExclude;
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
@Table(name = "services")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    private String serviceName;

    private Integer serviceDuration;

    private Double price;

    private String invoiceName;

    private String pkwiu;

    private String timeUnit;

    private String vat;

    private String basisForVatExemption;

    private Boolean isDefaultService=Boolean.FALSE;

    private Boolean isDeleted=Boolean.FALSE;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Set<Event> events = new HashSet<>();

    @ToStringExclude
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "services", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @OneToMany( fetch = FetchType.LAZY)
    private Set<Group> groups;

    public Service(String serviceName, Integer serviceDuration, Double price, String invoiceName, String pkwiu, String timeUnit, String vat, String basisForVatExemption) {
        this.serviceName = serviceName;
        this.serviceDuration = serviceDuration;
        this.price = price;
        this.invoiceName = invoiceName;
        this.pkwiu = pkwiu;
        this.timeUnit = timeUnit;
        this.vat = vat;
        this.basisForVatExemption = basisForVatExemption;
    }
}

package codepred.client;

import codepred.account.User;
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
@Table(name = "client_details")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class ClientDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    private String name;
    private String lastname;
    private String streetAndNumber;
    private String postcode;
    private String province;
    private String phoneNumber;
    private String color;
    private String meetingLink;
    private String storageLink;
    private Integer minimumChangeTime;
    @Column(length = 1000)
    private String additionalInformation;
    private byte[] clientPhoto;
    private Boolean isBusinessInvoice;
    private Boolean isAvailableToChangeReservation;
    private Boolean isAvailableToCancelReservation;
    private String nip;
    private String regon;
    private Integer tutorId;
}

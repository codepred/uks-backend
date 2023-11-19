package codepred.tutor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "tutor_details")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Setter
@EntityListeners(AuditingEntityListener.class)
public class TutorDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    private String activityType;

    private String name;

    private String surname;

    private String phoneNumber;

    private String nip;

    private String regon;

    private String companyName;

    private String street;

    private String postCode;

    private String place;

    private String bankName;

    private String bankAccountNumber;

    private Boolean isServiceActive = Boolean.FALSE;

    private byte[] photo;

    public Integer getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getActivityType() {
        return activityType;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getNip() {
        return nip;
    }

    public String getRegon() {
        return regon;
    }

    public String getCompanyName() {
        if(companyName == null || companyName.equals("undefined")){
            return "";
        }
        return companyName;
    }

    public String getStreet() {
        return street;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getPlace() {
        return place;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public Boolean getServiceActive() {
        return isServiceActive;
    }

    public byte[] getPhoto() {
        return photo;
    }
}

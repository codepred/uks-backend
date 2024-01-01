package codepred.documents;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "product")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "name")
    private String name;

    @Column(name = "amount")
    private String amount;

    @Column(name = "price")
    private String price;

    // TODO: Delete this field, not used
    @Column(name = "total_price")
    private String totalPrice;

    @Column(name = "uks_number")
    private String uksNumber;

    @Column(name = "uks_path")
    private String uksPath;

    @Column(name = "uks_file_number")
    private Integer uksFileNumber;

}

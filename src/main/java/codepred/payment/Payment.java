package codepred.payment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import codepred.account.User;
import codepred.invoice.Invoice;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "payment")
@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @OneToMany(mappedBy="payment", fetch = FetchType.LAZY)
    private List<Invoice> invoices;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private User user;

    private String typeOfDocument;
    private String invoiceNumber;
    private LocalDateTime dateOfIssue;
    private String placeOfIssue;
    private LocalDateTime dateOfSale;
    private String sellerNameAndSurname;
    private String sellerAddress;
    private String sellerNip;
    private String sellerEmail;
    private String sellerBankName;
    private String sellerBankAccountNumber;
    private Integer buyerId;
    private String buyerType;
    private String buyerName;
    private String nip;
    private String street;
    private String zip;
    private String city;
    private String typeOfPayment;
    private Integer dueDate;
    private String status;
    private Float valueToBePaid;
    private Float amountPaid;
    private String vatExemptionReason;

    private Float netto;
    private Float vatValue;
    private Float brutto;
    private Float checkValue;

}

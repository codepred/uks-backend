package codepred.payment.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
@Getter
@Setter
public class PaymentFullDataDto {

    private String typeOfDocument;
    private String invoiceNumber;
    private LocalDateTime dateOfIssue;
    private String placeOfIssue;
    private LocalDateTime dateOfSale;
    @NotNull
    @NotEmpty
    private String buyerName;
    @NotNull
    @NotEmpty
    private String buyerType;
    @NotNull
    @NotEmpty
    private Integer buyerId;
    private String nip;
    private String street;
    private String zip;
    private String city;
    private String typeOfPayment;
    private Integer dueDate;
    private String status;
    private String vatExemptionReason;

    private Float netto;
    private Float vatValue;
    private Float brutto;
    private Float checkValue;
    private Float amountPaid;
    private Float valueToBePaid;

}

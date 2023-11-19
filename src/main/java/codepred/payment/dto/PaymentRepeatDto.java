package codepred.payment.dto;

import codepred.invoice.dto.InvoiceDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class PaymentRepeatDto {
    private String typeOfDocument;
    private String invoiceNumber;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate dateOfIssue;
    private String placeOfIssue;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDateTime dateOfSale;
    private String sellerNameAndSurname;
    private String sellerAddress;
    private String sellerNip;
    private String sellerEmail;
    private String sellerBankName;
    private String sellerBankAccountNumber;
    private String buyerName;
    private String buyerType;
    private Integer buyerId;
    private String nip;
    private String street;
    private String zip;
    private String city;
    private String typeOfPayment;
    private Integer dueDate;
    private String status;
    private Float amountPaid;
    private String vatExemptionReason;

    private List<InvoiceDto> invoiceItems;
}

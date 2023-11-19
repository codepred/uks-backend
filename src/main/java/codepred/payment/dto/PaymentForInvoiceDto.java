package codepred.payment.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class PaymentForInvoiceDto {

    private Integer id;
    private String name;
    private String pkwiu;
    private Integer number;
    private String unit;
    private Double price;
    private String vat;
    private Float valuePaid;

}

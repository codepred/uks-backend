package codepred.payment.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentChangeStatusDto {
    private Integer paymentId;
    private String newStatus;
}

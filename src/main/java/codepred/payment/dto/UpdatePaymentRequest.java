package codepred.payment.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class UpdatePaymentRequest {
    private Integer attendanceId;
    private Float valuePaid;
}

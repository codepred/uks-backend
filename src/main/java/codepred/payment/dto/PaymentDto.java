package codepred.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class PaymentDto {

    private Integer id;
    private String number;
    private String client;
    private String status;
    private Float value;
    private Float paid;
    private Float toBePaid;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDateTime date;

}

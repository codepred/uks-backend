package codepred.payment.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentReadDto {

    private long id;
    private String numer;
    private Integer clientId;
    private String clientType;
    private String client;
    private String status;
    private Float value;
    private Float paid;
    private Float toBePaid;
    private LocalDateTime date;

}

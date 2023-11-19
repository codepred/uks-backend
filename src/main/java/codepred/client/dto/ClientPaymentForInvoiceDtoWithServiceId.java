package codepred.client.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class ClientPaymentForInvoiceDtoWithServiceId {

    private Integer id;
    private Integer serviceId;
    private String name;
    private String pkwiu;
    private Integer number;
    private String unit;
    private Double price;
    private String vat;
    private Float valuePaid;
}

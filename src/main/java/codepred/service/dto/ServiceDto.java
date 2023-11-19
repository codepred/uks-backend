package codepred.service.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ServiceDto {
    private Integer id;
    private String serviceData;

    private Integer serviceDuration;
    private Double servicePrice;
}

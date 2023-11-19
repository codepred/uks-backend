package codepred.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetailsPerTutorDto {

    private Integer tutorId;
    private String customerName;
    private String customerType;
}

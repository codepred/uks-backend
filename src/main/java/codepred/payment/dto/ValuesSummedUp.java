package codepred.payment.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ValuesSummedUp {

    private Float netto;
    private Float vatValue;
    private Float brutto;
    private Float checkValue;
    private Float amountPaid;
    private Float valueToBePaid;

}

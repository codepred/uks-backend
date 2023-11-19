package codepred.invoice.dto;

import codepred.payment.dto.ChosenServiceDataDto;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class InvoiceDto {

    @NotNull
    @NotEmpty
    private String name;
    private ChosenServiceDataDto chosenServiceData;
    private String pkwiu;
    private Integer amount;
    private String unit;
    private Float priceNetto;
    private String vat;
    private Float fullPriceNetto;
    private Float fullPriceBrutto;
    private Float checkPrice;
    private Float checkValue;

}

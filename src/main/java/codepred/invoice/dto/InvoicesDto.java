package codepred.invoice.dto;

import codepred.payment.dto.PaymentFullDataDto;
import codepred.payment.dto.ValuesSummedUp;
import lombok.Data;

import java.util.List;


@Data
public class InvoicesDto {

    private List<InvoiceDto> invoiceItems;
    private PaymentFullDataDto formData;
    private ValuesSummedUp summedUpValues;
}

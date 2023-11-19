package codepred.common.mapper;

import codepred.invoice.Invoice;
import codepred.invoice.dto.InvoiceDto;
import codepred.payment.Payment;
import codepred.payment.dto.PaymentRepeatDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    PaymentRepeatDto toPaymentRepeatDto(Payment payment);

    InvoiceDto toInvoiceDto(Invoice invoice);
}

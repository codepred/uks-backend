package codepred.payment;

import codepred.account.User;
import codepred.client.ClientDetailsRepository;
import codepred.client.ClientRepository;
import codepred.client.ClientService;
import codepred.client.dto.ClientDto;
import codepred.common.mapper.PaymentMapper;
import codepred.common.util.ResponseObject;
import codepred.exception.CustomException;
import codepred.invoice.Invoice;
import codepred.invoice.InvoiceRepository;
import codepred.invoice.dto.InvoiceDto;
import codepred.invoice.dto.InvoicesDto;
import codepred.payment.dto.PaymentReadDto;
import codepred.payment.dto.PaymentRepeatDto;
import codepred.service.ServiceRepository;
import codepred.tutor.TutorRepository;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class PaymentService {
    @Autowired
    private ClientDetailsRepository clientDetailsRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    ClientService clientService;

    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    private TemplateEngine templateEngine;

    public List<PaymentReadDto> getList(User user) {
        List<Payment> payments = paymentRepository.getByUser(user.getId());
        List<PaymentReadDto> list = new ArrayList<>();
        if (payments.isEmpty()) {
            return new ArrayList<>();
        }
        for (Payment payment : payments) {
            PaymentReadDto PaymentReadDto = new PaymentReadDto();
            PaymentReadDto.setId(payment.getId());
            PaymentReadDto.setNumer(payment.getInvoiceNumber());
            PaymentReadDto.setClientId(payment.getBuyerId());
            PaymentReadDto.setClientType(payment.getBuyerType());
            PaymentReadDto.setClient(payment.getBuyerName());
            PaymentReadDto.setStatus(payment.getStatus());

            if(payment.getTypeOfDocument().equals("invoice")){
                PaymentReadDto.setValue(payment.getBrutto());
            }
            else {
                PaymentReadDto.setValue(payment.getCheckValue());
            }
            PaymentReadDto.setPaid(payment.getAmountPaid());
            PaymentReadDto.setToBePaid(payment.getValueToBePaid());
            PaymentReadDto.setDate(payment.getDateOfIssue());
            list.add(PaymentReadDto);
        }

        return list;
    }

    public ResponseObject createPayment(InvoicesDto InvoicesDto, User user) {

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setTypeOfDocument(InvoicesDto.getFormData().getTypeOfDocument());
        payment.setInvoiceNumber(InvoicesDto.getFormData().getInvoiceNumber());
        payment.setDateOfIssue(InvoicesDto.getFormData().getDateOfIssue());
        payment.setPlaceOfIssue(InvoicesDto.getFormData().getPlaceOfIssue());
        payment.setDateOfSale(InvoicesDto.getFormData().getDateOfSale());
        payment.setBuyerName(InvoicesDto.getFormData().getBuyerName());
        payment.setBuyerType(InvoicesDto.getFormData().getBuyerType());
        payment.setBuyerId(InvoicesDto.getFormData().getBuyerId());
        payment.setNip(InvoicesDto.getFormData().getNip());
        payment.setStreet(InvoicesDto.getFormData().getStreet());
        payment.setZip(InvoicesDto.getFormData().getZip());
        payment.setCity(InvoicesDto.getFormData().getCity());
        payment.setTypeOfPayment(InvoicesDto.getFormData().getTypeOfPayment());
        payment.setDueDate(InvoicesDto.getFormData().getDueDate());
        payment.setStatus(InvoicesDto.getFormData().getStatus());
        payment.setAmountPaid(InvoicesDto.getFormData().getAmountPaid());
        payment.setVatExemptionReason(InvoicesDto.getFormData().getVatExemptionReason());
        payment.setValueToBePaid(InvoicesDto.getFormData().getValueToBePaid());
        payment.setSellerEmail(user.getEmail());
        payment.setSellerNameAndSurname(user.getTutorDetails().getName() + " " + user.getTutorDetails().getSurname());
        payment.setSellerBankName(user.getTutorDetails().getBankName());
        payment.setSellerBankAccountNumber(user.getTutorDetails().getBankAccountNumber());
        payment.setSellerAddress(user.getTutorDetails().getPlace());
        payment.setSellerNip(user.getTutorDetails().getNip());

        payment.setNetto(InvoicesDto.getSummedUpValues().getNetto());
        payment.setVatValue(InvoicesDto.getSummedUpValues().getVatValue());
        payment.setBrutto(InvoicesDto.getSummedUpValues().getBrutto());
        payment.setCheckValue(InvoicesDto.getSummedUpValues().getCheckValue());
        payment.setAmountPaid(InvoicesDto.getSummedUpValues().getAmountPaid());
        payment.setValueToBePaid(InvoicesDto.getSummedUpValues().getValueToBePaid());

        payment = paymentRepository.saveAndFlush(payment);


        List<Invoice> invoiceList = new ArrayList<>();
        for (InvoiceDto invoiceDto : InvoicesDto.getInvoiceItems()) {
            Invoice invoice = createInvoice(invoiceDto, payment);
            invoiceList.add(invoice);
        }
        payment.setInvoices(invoiceList);
        paymentRepository.save(payment);
        return new ResponseObject(HttpStatus.ACCEPTED, "PAYMENT_SUCCESSFULLY_SAVE", null);
    }

    public Invoice createInvoice(InvoiceDto invoiceDto, Payment payment) {
        var service = serviceRepository.findServiceById(invoiceDto.getChosenServiceData().getId()).get();
        Invoice invoice = new Invoice();
        invoice.setPayment(payment);
        invoice.setName(service.getServiceName());
        invoice.setPkwiu(invoiceDto.getPkwiu());
        invoice.setAmount(invoiceDto.getAmount());
        invoice.setUnit(invoiceDto.getUnit());
        invoice.setPriceNetto(invoiceDto.getPriceNetto());
        invoice.setVat(invoiceDto.getVat());
        setVatValues(invoiceDto, invoice);
        invoice.setCheckPrice(invoiceDto.getCheckPrice());
        invoice.setCheckValue(invoiceDto.getCheckValue());
        invoice = invoiceRepository.saveAndFlush(invoice);
        return invoice;
    }

    private void setVatValues(InvoiceDto invoiceDto, Invoice invoice) {
        if(invoiceDto.getFullPriceBrutto()!=null&& invoiceDto.getFullPriceNetto()!=null){
            invoice.setFullPriceNetto(invoiceDto.getFullPriceNetto());
            invoice.setVatAmount(invoiceDto.getFullPriceBrutto()- invoiceDto.getFullPriceNetto());
            invoice.setFullPriceBrutto(invoiceDto.getFullPriceBrutto());
        }
    }

    public List<ClientDto> getAllClients(User tutor) {
        log.debug("PaymentService ==> getAllClients() - start: user = {}", tutor);
        var studentIds = paymentRepository.getClientIds(tutor.getId());
        var groups = paymentRepository.getAllGroups(tutor.getId());
        var companies = paymentRepository.getAllCompanies(tutor.getId());

        List<ClientDto> studentDtos = studentIds.stream()
                .map(studentId -> {
                    Optional<User> client = clientRepository.findStudentById(studentId);
                    if(client.isEmpty()) return null;
                    var clientDetailsForGivenTutor = clientDetailsRepository.findByTutorIdAndClientId(client.get().getId(), tutor.getId());
                    return ClientDto.builder()
                            .id(studentId)
                            .type("STUDENT")
                            .name(clientDetailsForGivenTutor.getName() + " " + clientDetailsForGivenTutor.getLastname())
                            .build();
                })
                .collect(Collectors.toList());
        studentDtos = studentDtos.stream().filter(Objects::nonNull).collect(Collectors.toList());

        List<ClientDto> groupDtos = groups.stream()
                .map(group -> ClientDto.builder()
                        .id((Integer) group[0])
                        .type("GROUP")
                        .name((String) group[1])
                        .build())
                .collect(Collectors.toList());

        List<ClientDto> companyDtos = companies.stream()
                .map(company -> ClientDto.builder()
                        .id((Integer) company[0])
                        .type("COMPANY")
                        .name((String) company[1])
                        .build())
                .collect(Collectors.toList());

        var response = Stream.of(studentDtos, groupDtos, companyDtos)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        log.debug("PaymentService ==> getAllClients() - end: response = {}", response);
        return response;
    }

    public byte[] getInvoice(User user, Integer paymentId) {
        var paymentOptional = paymentRepository.findByIdWithInvoices(paymentId);
        Payment updatedPayment = paymentOptional.orElseThrow(() -> new CustomException("PAYMENT_NOT_FOUND", HttpStatus.BAD_REQUEST));

        log.info("PaymentService ==> getInvoice() - end: OK");
        try {
            return generateInvoice(updatedPayment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public ResponseObject removePayment(User user, Integer paymentId) {
        var payment = paymentRepository.findPaymentByIdAndTutor(paymentId, user.getId()).orElseThrow(() -> new CustomException("PAYMENT_NOT_FOUND", HttpStatus.BAD_REQUEST));
        paymentRepository.removeInvoiceByPayment(payment);
        paymentRepository.removePaymentById(paymentId);
        return new ResponseObject(HttpStatus.OK, "PAYMENT_SUCCESSFULLY_REMOVED", null);
    }

    @Transactional
    public PaymentRepeatDto repeatPayment(User user, Integer paymentId) {
        var checkPayment = paymentRepository.findPaymentByIdAndTutor(paymentId, user.getId())
                .orElseThrow(() -> new CustomException("PAYMENT_NOT_FOUND", HttpStatus.BAD_REQUEST));
        var paymentFromDb = paymentRepository.findPaymentById(checkPayment)
                .orElseThrow(() -> new CustomException("PAYMENT_NOT_FOUND", HttpStatus.BAD_REQUEST));

        PaymentRepeatDto response = PaymentMapper.INSTANCE.toPaymentRepeatDto(paymentFromDb);

        List<InvoiceDto> invoiceDtos = paymentFromDb.getInvoices()
                .stream()
                .map(PaymentMapper.INSTANCE::toInvoiceDto)
                .collect(Collectors.toList());

        response.setInvoiceItems(invoiceDtos);

        return response;
    }

    public ResponseObject changeStatus(User user, Integer paymentId, String status) {
        var payment = paymentRepository.findByIdAndUser(paymentId, user).orElseThrow(() -> new CustomException("PAYMENT_NOT_FOUND", HttpStatus.BAD_REQUEST));
        payment.setStatus(status);
        updatePaidAmount(payment,status);
        paymentRepository.save(payment);
        return new ResponseObject(HttpStatus.OK, "STATUS_CHANGED", null);
    }

    private void updatePaidAmount(Payment payment, String status) {
        switch (status) {
            case "paid":
                payment.setAmountPaid(payment.getValueToBePaid());
                payment.setValueToBePaid(0F);
                break;
            case "issued":
                payment.setValueToBePaid(payment.getAmountPaid());
                payment.setAmountPaid(0F);
                break;
            case "partlyPaid":
                break;
        }
    }

    private byte[] generateInvoice(Payment payment) throws IOException, DocumentException {

        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }

        Context context = new Context();
        String processedHtml;

        Float amountPaid = payment.getAmountPaid();
        String formattedAmountPaid = String.format("%.2f", amountPaid);
        context.setVariable("amountPaid",formattedAmountPaid);
        context.setVariable("payment", payment);

        if (payment.getTypeOfDocument().equals("check")) {
            setCheckFormattedValuesToContext(payment, context);
            processedHtml = templateEngine.process("check_template", context);

        } else {

            setInvoiceFormattedValuesToContext(payment, context);
            processedHtml = templateEngine.process("invoice_template", context);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        ITextFontResolver fontResolver = renderer.getFontResolver();
        fontResolver.addFont("fonts/LiberationSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fontResolver.addFont("fonts/LiberationSans-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fontResolver.addFont("fonts/LiberationSans-Italic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fontResolver.addFont("fonts/LiberationSans-BoldItalic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        renderer.setDocumentFromString(processedHtml);
        renderer.layout();
        renderer.createPDF(out);

        return out.toByteArray();
    }

    private void setCheckFormattedValuesToContext(Payment payment, Context context) {

        Float summary = 0F;
        for (Invoice invoice : payment.getInvoices()) {
            summary += invoice.getCheckValue();
        }

        LocalDateTime dateOfIssue = payment.getDateOfIssue();
        int dueDateInterval = payment.getDueDate();

        LocalDateTime dueDate = dateOfIssue.plus(dueDateInterval, ChronoUnit.DAYS);
        context.setVariable("dueDate", dueDate);

        Float toPay = summary - payment.getAmountPaid();
        String formattedToPay = String.format("%.2f", toPay);
        String formattedSummary = String.format("%.2f", summary);

        context.setVariable("toPay", formattedToPay);
        context.setVariable("summary", formattedSummary);

        List<String> formattedPriceNettoValues = payment.getInvoices().stream()
                .map(Invoice::getPriceNetto)
                .map(nettoValue -> String.format("%.2f", nettoValue))
                .collect(Collectors.toList());

        List<String> formattedCheckValues = payment.getInvoices().stream()
                .map(Invoice::getCheckValue)
                .map(checkValue -> String.format("%.2f", checkValue))
                .collect(Collectors.toList());

        context.setVariable("nettoValues", formattedPriceNettoValues);
        context.setVariable("checkValues", formattedCheckValues);
    }

    private void setInvoiceFormattedValuesToContext(Payment payment, Context context) {


        Float summaryNetPrice = 0F;
        Float summaryBruttoPrice = 0F;

        for (Invoice invoice : payment.getInvoices()) {
            summaryNetPrice += invoice.getFullPriceNetto();
            summaryBruttoPrice += invoice.getFullPriceBrutto();
        }

        LocalDateTime dateOfIssue = payment.getDateOfIssue();
        int dueDateInterval = 14;
        if( payment.getDueDate() != null) {
            dueDateInterval = payment.getDueDate();
        }

        LocalDateTime dueDate = dateOfIssue.plus(dueDateInterval, ChronoUnit.DAYS);
        Float summaryVat = summaryBruttoPrice - summaryNetPrice;
        Float toPay = summaryBruttoPrice - payment.getAmountPaid();
        String formattedToPay = String.format("%.2f", toPay);
        String formattedNetPrice = String.format("%.2f", summaryNetPrice);
        String formattedBruttoPrice = String.format("%.2f", summaryBruttoPrice);
        String formattedVat = String.format("%.2f", summaryVat);

        List<String> formattedInvoiceVatValues = payment.getInvoices().stream()
                .map(Invoice::getVatAmount)
                .map(vatValue -> String.format("%.2f", vatValue))
                .collect(Collectors.toList());

        List<String> formattedPriceNettoValues = payment.getInvoices().stream()
                .map(Invoice::getFullPriceNetto)
                .map(nettoValue -> String.format("%.2f", nettoValue))
                .collect(Collectors.toList());

        List<String> formattedPriceBruttoValues = payment.getInvoices().stream()
                .map(Invoice::getFullPriceBrutto)
                .map(bruttoValue -> String.format("%.2f", bruttoValue))
                .collect(Collectors.toList());

        context.setVariable("vatValues",formattedInvoiceVatValues);
        context.setVariable("priceNettoValues",formattedPriceNettoValues);
        context.setVariable("priceBruttoValues",formattedPriceBruttoValues);
        context.setVariable("summaryNetPrice", formattedNetPrice);
        context.setVariable("summaryBruttoPrice", formattedBruttoPrice);
        context.setVariable("summaryVat", formattedVat);
        context.setVariable("toPay", formattedToPay);
        context.setVariable("dueDate", dueDate);
    }

}

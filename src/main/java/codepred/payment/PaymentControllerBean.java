package codepred.payment;

import codepred.client.dto.ClientDto;
import codepred.common.util.ResponseObject;
import codepred.invoice.dto.InvoicesDto;
import codepred.payment.dto.PaymentChangeStatusDto;
import codepred.payment.dto.PaymentReadDto;
import codepred.payment.dto.PaymentRepeatDto;
import codepred.tutor.TutorService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/tutor/payments/", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Api(value = "Tutor, part for payments", tags = "Tutor-payments")
@Tag(name = "Tutor-payments", description = "Tutor-payments API")
@CrossOrigin
public class PaymentControllerBean implements PaymentController{

    @Autowired
    TutorService tutorService;

    @Autowired
    PaymentService paymentService;

    @Override
    @CrossOrigin
    public List<PaymentReadDto> getPaymentList(HttpServletRequest request) {
        var tutor = tutorService.getTutorByToken(request);
        return paymentService.getList(tutor);
    }

    @Override
    @CrossOrigin
    public ResponseObject createPayment(HttpServletRequest request, @Valid InvoicesDto invoicesDto) {
        var tutor = tutorService.getTutorByToken(request);
        return paymentService.createPayment(invoicesDto, tutor);
    }

    @Override
    @CrossOrigin
    public List<ClientDto> getAllClients(HttpServletRequest request){
        var tutor = tutorService.getTutorByToken(request);
        return paymentService.getAllClients(tutor);
    }

    @Override
    @CrossOrigin
    public ResponseEntity<byte[]> generateInvoice(HttpServletRequest request, Integer invoice) {
        var tutor = tutorService.getTutorByToken(request);
        byte[] invoicePdf = paymentService.getInvoice(tutor, invoice);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=invoice.pdf")
                .header("Content-Type", "application/pdf")
                .body(invoicePdf);
    }

    @Override
    @CrossOrigin
    public ResponseObject removePayment(HttpServletRequest request, Integer id) {
        var tutor = tutorService.getTutorByToken(request);
        return paymentService.removePayment(tutor, id);
    }

    @Override
    @CrossOrigin
    public PaymentRepeatDto repeatEvent(HttpServletRequest request, Integer paymentId){
        var tutor = tutorService.getTutorByToken(request);
        return paymentService.repeatPayment(tutor,paymentId);
    }

    @Override
    @CrossOrigin
    public ResponseObject changeStatus(HttpServletRequest request, PaymentChangeStatusDto paymentChangeStatusDto){
        var tutor=tutorService.getTutorByToken(request);
        return paymentService.changeStatus(tutor, paymentChangeStatusDto.getPaymentId(), paymentChangeStatusDto.getNewStatus());
    }

}

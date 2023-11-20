package codepred.documents;

import codepred.config.EmailValidator;
import com.lowagie.text.DocumentException;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvoiceController {

    private final MailService mailService;
    private final DocumentService pdfService;

    public InvoiceController(final MailService mailService, final DocumentService pdfService) {
        this.mailService = mailService;
        this.pdfService = pdfService;
    }

    @PostMapping("/invoice/create-pdf")
    public ResponseEntity sendEmail(@RequestBody InvoiceData invoicedata) throws IOException, DocumentException {
        if(!EmailValidator.isValidEmail(invoicedata.getEmail())){
            return ResponseEntity.status(400).build();
        }

        final var pdfDocument = pdfService.generateInvoice(invoicedata);
        mailService.sendEmailWithAttachment(invoicedata.getEmail(), "Umowa kupna-sprzedaży", "Dziękuję za transakcję. W załączniku przesyłam umowę kupna-sprzedaży. \n \n "
            + "Thank you for the transaction. Sale agreement document attached to message.", pdfDocument, invoicedata.getName());
        return null;
    }

}
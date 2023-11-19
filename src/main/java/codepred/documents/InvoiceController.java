package codepred.documents;

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
        final var pdfDocument = pdfService.generateInvoice(invoicedata);
        mailService.sendEmailWithAttachment("kaczmarek.jacek10@gmail.com", "Umowa kupna-sprzedaży", "Witaj,"
            + "w załączniku przesyłamy dokument kupna-sprzedaży. Pozdrawiamy, Zespół buty", pdfDocument);
        return null;
    }

}
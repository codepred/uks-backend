package codepred.documents;

import codepred.config.EmailValidator;
import com.lowagie.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class InvoiceController {

    @Value("${invoice_path}")
    private String invoicePath;
    private final MailService mailService;
    private final DocumentService pdfService;

    public InvoiceController(final MailService mailService, final DocumentService pdfService) {
        this.mailService = mailService;
        this.pdfService = pdfService;
    }

    @PostMapping("/invoice/create-pdf")
    public ResponseEntity sendEmail(@RequestBody InvoiceData invoicedata) throws IOException, DocumentException {
        if (!EmailValidator.isValidEmail(invoicedata.getEmail())) {
            return ResponseEntity.status(400).build();
        }
        InvoiceEntity invoice = pdfService.saveInvoice(invoicedata);
        List<byte[]> invoicesPdf = new ArrayList<>();


        for(Product product : invoicedata.getProductList()){
            InvoiceData tempInvoiceData = new InvoiceData();
            tempInvoiceData = invoicedata;
            tempInvoiceData.setProductList(List.of(product));
            invoicesPdf.add(pdfService.generateInvoice(tempInvoiceData, invoice));
        }
        mailService.sendEmailWithAttachment(invoicedata.getEmail(),
                                            "Umowa kupna-sprzedaży",
                                            "Dziękuję za transakcję. W załączniku przesyłam umowę kupna-sprzedaży. \n \n "
                                                + "Thank you for the transaction. Sale agreement document attached to message.",
                                            invoicesPdf,
                                            invoicedata.getName(),
                                            invoice.getId().toString());
        return null;
    }

    @GetMapping("/display/{fileName}")
    public ResponseEntity<InputStreamResource> displayEmail(@PathVariable("fileName") String fileName)
        throws FileNotFoundException {
        Path file = Paths.get(invoicePath, fileName);

        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName);

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new InputStreamResource(new FileInputStream(file.toFile())));
    }

    @GetMapping("/download/{fileName}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),
    })
    public ResponseEntity<byte[]> downloadBookmark(HttpServletRequest request, @PathVariable("fileName") String fileName)
        throws IOException {
        byte[] invoicePdf = pdfService.download(fileName);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=invoice.pdf")
            .header("Content-Type", "application/pdf")
            .body(invoicePdf);
    }

}
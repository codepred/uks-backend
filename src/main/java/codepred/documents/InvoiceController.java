package codepred.documents;

import codepred.common.FileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import codepred.config.EmailValidator;
import com.lowagie.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class InvoiceController {

    private final MailService mailService;
    private final DocumentService documentService;
    private final FileService fileService;
    private final InvoiceService invoiceService;

    private final ProductRepository productRepository;

    @PostMapping(value = "/invoice/create-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> sendEmail(@RequestParam("username") String username,
                                            @RequestParam("email") String email,
                                            @RequestParam("date") String date,
                                            @RequestParam("name") String name,
                                            @RequestParam("street") String street,
                                            @RequestParam("aptNumber") String aptNumber,
                                            @RequestParam("zip") String zip,
                                            @RequestParam("city") String city,
                                            @RequestParam("paymentMethod") String paymentMethod,
                                            @RequestParam("currency") String currency,
                                            @RequestParam("signature") String signaturePhoto,
                                            @RequestParam("productList") String productListString)
        throws IOException, DocumentException {

        final var invoicedata = new InvoiceData();
        invoicedata.setUsername(username);
        invoicedata.setEmail(email);
        invoicedata.setDate(date);
        invoicedata.setName(name);
        invoicedata.setStreet(street);
        invoicedata.setAptNumber(aptNumber);
        invoicedata.setZip(zip);
        invoicedata.setCity(city);
        invoicedata.setPaymentMethod(paymentMethod);
        invoicedata.setCurrency(currency);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Product> productList = objectMapper.readValue(productListString, new TypeReference<List<Product>>() {
        });
        productList = invoiceService.addDuplicatedProducts(productList);
        invoicedata.setProductList(productList);

        if (!EmailValidator.isValidEmail(email)) {
            return ResponseEntity.status(400).body("INVALID EMAIL");
        }

        fileService.saveFile(signaturePhoto);
        InvoiceEntity invoice = invoiceService.saveInvoice(invoicedata);
        List<byte[]> invoicesPdf = new ArrayList<>();

        final var fileNumberValue = productRepository.getLastUksFileNumberByClientName(name);
        var fileNumber = (fileNumberValue != null) ? fileNumberValue + 1 : 0;

        for (Product product : productList) {
            invoicesPdf.add(documentService.generateInvoiceDocument(invoicedata, product, signaturePhoto, fileNumber, invoice));
            fileNumber++;
        }

        mailService.sendEmailWithAttachment(email,
                                            "Umowa kupna-sprzedaży",
                                            "Dziękuję za transakcję. W załączniku przesyłam umowę kupna-sprzedaży. \n \n "
                                                + "Thank you for the transaction. Sale agreement document attached to message.",
                                            invoicesPdf,
                                            name);
        return ResponseEntity.ok().body("Invoices were generated");
    }

    @GetMapping("/display/{fileName}")
    public ResponseEntity<InputStreamResource> displayEmail(@PathVariable("fileName") String fileName)
        throws FileNotFoundException {
        return documentService.getPdfDocument(fileName);
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),})
    public ResponseEntity<byte[]> downloadBookmark(HttpServletRequest request, @PathVariable("fileName") String fileName)
        throws IOException {
        final var invoicePdf = documentService.download(fileName);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=invoice.pdf")
            .header("Content-Type", "application/pdf")
            .body(invoicePdf);
    }

    @PostMapping("/invoice/all")
    public ResponseEntity<Object> getAllInvoices(@RequestBody PeriodData periodData) {
        return ResponseEntity.status(200).body(invoiceService.getAllInvoices(periodData));
    }

    @DeleteMapping("/invoice/delete/{id}")
    public ResponseEntity<Object> deleteInvoice(@PathVariable("id") Integer id) {
        return ResponseEntity.status(200).body(invoiceService.deleteProduct(id));
    }

    @PostMapping("/invoice/month")
    public ResponseEntity<InputStreamResource> getMonthlyUks(@RequestBody MonthlyUksData monthlyUksData) throws IOException {
        final var fileName = documentService.generateMonthInvoice(monthlyUksData);
        return documentService.getPdfDocument(fileName);
    }

}
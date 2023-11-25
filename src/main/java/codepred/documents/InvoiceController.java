package codepred.documents;

import java.io.FileOutputStream;
import net.coobird.thumbnailator.Thumbnails;
import codepred.config.EmailValidator;
import com.lowagie.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.File;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/invoice/create-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity sendEmail(@RequestParam("username") String username,
                                    @RequestParam("email") String email,
                                    @RequestParam("date") String date,
                                    @RequestParam("name") String name,
                                    @RequestParam("street") String street,
                                    @RequestParam("aptNumber") String aptNumber,
                                    @RequestParam("zip") String zip,
                                    @RequestParam("city") String city,
                                    @RequestParam("paymentMethod") String paymentMethod,
                                    @RequestParam("currency") String currency,
                                    @RequestParam("signature") MultipartFile signature,
                                    @RequestParam("productList") List<Product> productList)
        throws IOException, DocumentException {

        InvoiceData invoicedata = new InvoiceData();
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
        invoicedata.setProductList(productList);

        if (!EmailValidator.isValidEmail(email)) {
            return ResponseEntity.status(400).build();
        }

        saveSignature(signature);

        InvoiceEntity invoice = pdfService.saveInvoice(invoicedata);
        List<byte[]> invoicesPdf = new ArrayList<>();

        for (Product product : productList) {
            InvoiceData tempInvoiceData = new InvoiceData();
            tempInvoiceData = invoicedata;
            tempInvoiceData.setProductList(List.of(product));
            for (int i = 0; i < Integer.valueOf(product.getAmount()); i++) {
                invoicesPdf.add(pdfService.generateInvoice(tempInvoiceData, invoice, signature));
            }
        }

        mailService.sendEmailWithAttachment(email,
                                            "Umowa kupna-sprzedaży",
                                            "Dziękuję za transakcję. W załączniku przesyłam umowę kupna-sprzedaży. \n \n "
                                                + "Thank you for the transaction. Sale agreement document attached to message.",
                                            invoicesPdf,
                                            name,
                                            invoice.getId().toString());
        return ResponseEntity.ok().build();
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
    @Operation(summary = "Create customer", description = "Endpoint for create new password for customer. Also activate account.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "EMAIL_SENT"),
        @ApiResponse(responseCode = "400", description = "EMAIL_DOES_NOT_EXIST"),})
    public ResponseEntity<byte[]> downloadBookmark(HttpServletRequest request, @PathVariable("fileName") String fileName)
        throws IOException {
        byte[] invoicePdf = pdfService.download(fileName);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=invoice.pdf")
            .header("Content-Type", "application/pdf")
            .body(invoicePdf);
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) throws IOException {
        Path imagePath = Paths.get(invoicePath, imageName);

        File imageFile = imagePath.toFile();
        if (!imageFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Resize the image to 128x128 pixels
        File resizedImage = resizeImage(imageFile, 128, 128);

        Resource resource = new FileSystemResource(resizedImage);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG) // Change this according to your image type
            .body(resource);
    }

    private File resizeImage(File originalImage, int width, int height) throws IOException {
        File resizedImage = new File("resized_" + originalImage.getName()); // Create a new file for resized image

        Thumbnails.of(originalImage)
            .size(width, height)
            .outputFormat("jpg") // Change this according to your desired output format
            .toFile(resizedImage);

        return resizedImage;
    }

    public String saveSignature(MultipartFile signatureFile) {
        try {
            // Create the directory if it doesn't exist
            File signatureFolder = new File(invoicePath);
            if (!signatureFolder.exists()) {
                signatureFolder.mkdirs();
            }

            // Save the signature file to the filesystem
            File file = new File(invoicePath + signatureFile.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(signatureFile.getBytes());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return "Signature uploaded successfully!";
        } catch (Exception e) {
            return null;
        }
    }

}
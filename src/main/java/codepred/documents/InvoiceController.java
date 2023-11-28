package codepred.documents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileOutputStream;
import java.util.Base64;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final NumberRepository numberRepository;

    public InvoiceController(final MailService mailService, final DocumentService pdfService, final NumberRepository numberRepository) {
        this.mailService = mailService;
        this.pdfService = pdfService;
        this.numberRepository = numberRepository;
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
                                    @RequestParam("signature") String signaturePhoto,
                                    @RequestParam("productList") String productListString)
        throws IOException, DocumentException, InterruptedException {

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
        ObjectMapper objectMapper = new ObjectMapper();
        List<Product> productList = objectMapper.readValue(productListString, new TypeReference<List<Product>>() {});
        invoicedata.setProductList(productList);

        if (!EmailValidator.isValidEmail(email)) {
            return ResponseEntity.status(400).body("INVALID EMAIL");
        }

        saveFile(signaturePhoto);
        InvoiceEntity invoice = pdfService.saveInvoice(invoicedata);
        List<byte[]> invoicesPdf = new ArrayList<>();
        int number = 0;
        if(numberRepository.findAll().size() == 0){
            Number number1 = new Number();
            number1.setNumber(1);
            numberRepository.save(number1);
            number = 1;
        }
        else {
            Number numberFromDb = numberRepository.findAll().get(0);
            if(numberFromDb.getNumber() == null){
                numberFromDb.setNumber(1);
                numberRepository.save(numberFromDb);
                number = 1;
            }
            else {
                number = numberRepository.findAll().get(0).getNumber();
            }
        }

        for (Product product : productList) {
            InvoiceData tempInvoiceData = new InvoiceData();
            tempInvoiceData = invoicedata;
            tempInvoiceData.setProductList(List.of(product));
            for (int i = 0; i < Integer.valueOf(product.getAmount()); i++) {
                invoicesPdf.add(pdfService.generateInvoice(tempInvoiceData, invoice, signaturePhoto, username, number));
                number ++;
            }
        }

        Number updatedNumber = numberRepository.findAll().get(0);
        updatedNumber.setNumber(number);
        numberRepository.save(updatedNumber);

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
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) throws IOException {
        Path imagePath = Paths.get(invoicePath, imageName);

        File imageFile = imagePath.toFile();
        if (!imageFile.exists()) {
            return ResponseEntity.notFound().build();
        }

//        // Resize the image to 128x128 pixels
//        File resizedImage = resizeImage(imageFile, 500, 128);

        // Convert the resized image to a byte array
        byte[] resizedImageData = Files.readAllBytes(imageFile.toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Change this according to your image type

        return new ResponseEntity<>(resizedImageData, headers, HttpStatus.OK);
    }

    @PostMapping("/saveFile")
    public ResponseEntity<String> saveFileFromBytes(@RequestParam("fileData") String fileData) {
       saveFile(fileData);
            return ResponseEntity.ok("File saved successfully");
    }

    private File resizeImage(File originalImage, int width, int height) throws IOException {
        File outputDirectory = new File(invoicePath);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs(); // Create the directory if it doesn't exist
        }

        File resizedImage = new File(invoicePath, "resized_" + originalImage.getName());

        Thumbnails.of(originalImage)
            .size(width, height)
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

    public void saveFile(String fileData) {
        String filePath = invoicePath + "file.jpeg"; // Replace this with your desired file path

        try {
            // Decode the Base64 string to get the byte[]
            String[] parts = fileData.split(",");
            String base64String = parts[1]; // Assuming the Base64 content is at index 1

            // Decode the Base64 string to byte array
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);

            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(decodedBytes);
            fos.close();
        } catch (Exception e) {

        }
    }

}
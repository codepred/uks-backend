package codepred.documents;

import static codepred.common.DateUtil.getCurrentMonth;

import codepred.common.NumberService;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.time.Month;
import java.util.stream.Collectors;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
public class DocumentService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductRepository productRepository;

    @Value("${invoice_path}")
    private String invoicePath;

    public byte[] generateInvoiceDocument(InvoiceData invoiceData, Product product, String signature_blob, int fileNumber, InvoiceEntity invoice) throws IOException, DocumentException {
        Context context = new Context();
        String processedHtml;
        product.setUksNumber(numberService.generateID());

        String paymentType = null;
        if(invoiceData.getPaymentMethod().equals("transfer")){
            paymentType = "przelew/transfer";
        }
        if(invoiceData.getPaymentMethod().equals("blik")){
            paymentType = "BLIK";
        }
        if(invoiceData.getPaymentMethod().equals("paypal")){
            paymentType = "Paypal";
        }
        if(invoiceData.getPaymentMethod().equals("cash")){
            paymentType = "Gotowka/Cash";
        }
        invoiceData.setProductList(List.of(product));
        context.setVariable("place", invoiceData.getStreet() + " " + invoiceData.getCity());
        context.setVariable("date", invoiceData.getDate());
        context.setVariable("paymentType", paymentType);
        context.setVariable("buyerName", "GOT’EM STORE Mikołaj Maszner");
        context.setVariable("buyerAddress", "Żurawia 46");
        context.setVariable("buyerAddress1", "62-002 Złotniki");
        context.setVariable("buyerNip", "NIP: 9721301624");
        context.setVariable("finalPrice", invoiceData.getProductList().get(0).getPrice() + " " + invoiceData.getCurrency());
        context.setVariable("currency", invoiceData.getCurrency());
        context.setVariable("sellerName", "Imię i nazwisko: " + invoiceData.getName());
        context.setVariable("sellerAddress",
                            "Adres: " + invoiceData.getStreet() + " " + invoiceData.getAptNumber() + ", " + invoiceData.getZip()
                                + " " + invoiceData.getCity());
        context.setVariable("sellerEmail", "Email: " + invoiceData.getEmail());
        context.setVariable("invoiceData", invoiceData);
        context.setVariable("product",product);
        context.setVariable("signature_file", signature_blob);
        context.setVariable("invoiceNumber", "Sale agreement nr " + product.getUksNumber() + "-" + getCurrentMonth());
        processedHtml = templateEngine.process("invoice_template", context);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ITextRenderer renderer = getiTextRenderer(processedHtml);
        renderer.createPDF(out);

        String filePath = invoicePath + invoiceData.getName() + fileNumber + ".pdf";
        product.setUksPath(filePath);
        product.setUksFileNumber(fileNumber);
        product.setInvoice(invoice);
        productRepository.save(product);

        Path directoryPath = Paths.get(invoicePath);
        try {
            Files.createDirectories(directoryPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            renderer.createPDF(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    @NotNull
    private static ITextRenderer getiTextRenderer(String processedHtml) throws DocumentException, IOException {
        final ITextRenderer renderer = new ITextRenderer();
        ITextFontResolver fontResolver = renderer.getFontResolver();
        fontResolver.addFont("fonts/LiberationSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fontResolver.addFont("fonts/LiberationSans-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fontResolver.addFont("fonts/LiberationSans-Italic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fontResolver.addFont("fonts/LiberationSans-BoldItalic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        renderer.setDocumentFromString(processedHtml);
        renderer.layout();
        return renderer;
    }

    public String generateMonthInvoice(MonthlyUksData monthlyUksData) throws IOException {
        final var fileName = monthlyUksData.getMonth() + "-" + monthlyUksData.getYear() + ".pdf";
        List<Product> productList = productRepository.findAllById(monthlyUksData.getProductList());
        List<String> uksFilePath = productList.stream().map(Product::getUksPath).collect(Collectors.toList());

        generateMonthInvoicePdf(uksFilePath, fileName);
        return fileName;
    }

    public String generateMonthInvoicePdf(List<String> uksFilePath, String fileName) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        for (String filePath : uksFilePath) {
            merger.addSource(new File(filePath));
        }

        String outputFile = invoicePath + fileName;
        merger.setDestinationFileName(outputFile);
        merger.mergeDocuments();

        return outputFile;
    }

    public byte[] download(String fileName) throws IOException {
        byte[] fileContent = Files.readAllBytes(Path.of(invoicePath + fileName));
        return fileContent;
    }

    @NotNull
    ResponseEntity<InputStreamResource> getPdfDocument(String fileName) throws FileNotFoundException {
        final var file = Paths.get(invoicePath, fileName);
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        final var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=" + fileName);

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new InputStreamResource(new FileInputStream(file.toFile())));
    }

}

package codepred.documents;


import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private InvoiceRepository invoiceRepository;

    @Value("${invoice_path}")
    private String invoicePath;

    private InvoiceEntity saveInvoice(InvoiceData invoiceData) {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setUsername(invoiceData.getUsername());
        invoice.setCreatedAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    public byte[] generateInvoice(InvoiceData invoiceData) throws IOException, DocumentException {
        InvoiceEntity invoice = saveInvoice(invoiceData);

        Context context = new Context();
        String processedHtml;

        context.setVariable("paymentId", invoice.getId());
        context.setVariable("place", invoiceData.getStreet() + " " + invoiceData.getCity());
        context.setVariable("date", invoiceData.getDate());
        context.setVariable("paymentType", invoiceData.getPaymentMethod());
        context.setVariable("buyerName", "GOT’EM STORE Mikołaj Maszner");
        context.setVariable("buyerAddress", "Żurawia 46");
        context.setVariable("buyerAddress1", "62-002 Złotniki");
        context.setVariable("buyerNip", "NIP: 9721301624'");

        context.setVariable("sellerName", "Imię i nazwisko: " + invoiceData.getUsername());
        context.setVariable("sellerAddress", "Adres: " + invoiceData.getStreet() + " " + invoiceData.getAptNumber() + ", " + invoiceData.getZip() + " " + invoiceData.getCity());
        context.setVariable("sellerEmail", "Email: " + invoiceData.getEmail());
        context.setVariable("invoiceData", invoiceData);
        context.setVariable("public_domain","https://icons.iconarchive.com/icons/tribalmarkings/colorflow/128/signature-icon.png");

        processedHtml = templateEngine.process("invoice_template", context);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ITextRenderer renderer = getiTextRenderer(processedHtml);
        renderer.createPDF(out);

        String filePath = invoicePath + invoice.getId() + ".pdf";

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

}

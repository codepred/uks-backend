package codepred.documents;

import codepred.payment.Payment;
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
        processedHtml = templateEngine.process("invoice_template", context);

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

        String filePath = invoicePath + invoice.getId() + ".pdf";

// Create directories if they don't exist
        Path directoryPath = Paths.get(invoicePath);
        try {
            Files.createDirectories(directoryPath);
        } catch (IOException e) {
            // Handle directory creation exception
            e.printStackTrace();
        }

// Write the PDF content to the file
        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            renderer.createPDF(outputStream); // Write the generated PDF content to the file
        } catch (IOException e) {
            // Handle file writing exception
            e.printStackTrace();
        }

        return out.toByteArray();
    }

}

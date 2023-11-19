package codepred.documents;

import codepred.payment.Payment;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
public class DocumentService {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] generateInvoice(InvoiceData invoiceData) throws IOException, DocumentException {

        Context context = new Context();
        String processedHtml;


        context.setVariable("payment", invoiceData.getCurrency());
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

        return out.toByteArray();
    }

}

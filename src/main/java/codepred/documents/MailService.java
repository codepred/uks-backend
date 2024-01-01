package codepred.documents;

import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    private final String host = "smtp.gmail.com";

    public void sendEmailWithAttachment(String to, String subject, String body, List<byte[]> pdfDocuments, String name) {

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Create a multipart message
            Multipart multipart = new MimeMultipart();

            // Add text content to the email
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Dziękuję za transakcję. W załączniku przesyłam umowę kupna-sprzedaży. \n"
                                        + "Thank you for the transaction. Sale agreement document attached to message.");
            multipart.addBodyPart(messageBodyPart);

            // Attach multiple PDF files from byte arrays
            for (int i = 0; i < pdfDocuments.size(); i++) {
                byte[] pdfData = pdfDocuments.get(i);
                DataSource source = new ByteArrayDataSource(pdfData, "application/pdf");

                messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(name + "_" + (i + 1) + ".pdf"); // You can customize the file name here
                multipart.addBodyPart(messageBodyPart);
            }

            message.setContent(multipart);
            Transport.send(message);

            System.out.println("Email sent successfully with multiple PDF attachments!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}

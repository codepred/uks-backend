//package codepred.config;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.Properties;
//import javax.activation.DataHandler;
//import javax.mail.BodyPart;
//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.PasswordAuthentication;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeBodyPart;
//import javax.mail.internet.MimeMessage;
//import javax.mail.internet.MimeMultipart;
//import javax.mail.util.ByteArrayDataSource;
//import org.junit.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//public class TestEmail {
//
//
//    private String username = "tutorio.kontakt@gmail.com";
//
//    private String host = "smtp.gmail.com";
//
//    private Integer port = 587;
//
//    private String password = "ymgaaxwewqldqtjo";
//
//    @Test
//    public void testSendEmail() {
//        System.out.println("TEST");
//
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.imap.ssl.enable", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", host);
//        props.put("mail.smtp.port", port);
//        props.put("mail.smtp.user", username);
//        props.put("mail.smtp.email", username);
//        props.put("mail.smtp.password", password);
//
//        // Get the Session object.
//        Session session = Session.getInstance(props,
//                                              new javax.mail.Authenticator() {
//                                                  protected PasswordAuthentication getPasswordAuthentication() {
//                                                      return new PasswordAuthentication(username, password);
//                                                  }
//                                              });
//        try {
//
//            Message message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(username));
//
//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("kaczmarek.jacek10@gmail.com"));
//
//            message.setSubject("Tutorio");
//
//            MimeMultipart multipart = new MimeMultipart("related");
//
//            BodyPart messageBodyPart = new MimeBodyPart();
//            String html = "";
//            try {
//                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/verification_mail.html");
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    html += line;
//                }
//                reader.close();
//            } catch (IOException e) {
//                System.out.println("Error reading HTML file: " + e.getMessage());
//            }
//            html = html.replaceAll("verificationLink", "TEST MAIL"); // Replace "verificationLink" with the dynamic value you want to set
//
//            messageBodyPart.setContent(html, "text/html; charset=UTF-8");
//
//            multipart.addBodyPart(messageBodyPart);
//            messageBodyPart = new MimeBodyPart();
//            InputStream imageStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/tutorio-logo.png");
//            DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(imageStream, "image/png"));
//            messageBodyPart.setDataHandler(dataHandler);
//
//            messageBodyPart.setHeader("Content-ID", "<image>");
//
//            multipart.addBodyPart(messageBodyPart);
//
//            message.setContent(multipart);
//
//            Transport.send(message);
//        } catch (MessagingException ex) {
//            System.out.println(ex);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
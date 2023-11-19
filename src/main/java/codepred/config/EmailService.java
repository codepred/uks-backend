package codepred.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;


@Service
public class EmailService {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private Integer port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Async
    public void sendEmail(String email, String from, String subject, String text) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.user", username);
        props.put("mail.smtp.email", username);
        props.put("mail.smtp.password", password);

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

            message.setSubject("Tutorio");

            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();
            String html = "";
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/verification_mail.html");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error reading HTML file: " + e.getMessage());
            }
            html = html.replaceAll("verificationLink", text); // Replace "verificationLink" with the dynamic value you want to set

            setEmailConfigData(message, multipart, messageBodyPart, html);
        } catch (MessagingException ex) {
            System.out.println(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Async
    public void sendEmailToResetPassword(String email, String from, String subject, String text) {



        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

            message.setSubject("Tutorio");

            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();
            String html = "";
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/forgetpassword-template.html");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error reading HTML file: " + e.getMessage());
            }
            html = html.replaceAll("verificationLink", text); // Replace "06354" with the dynamic value you want to set

            setEmailConfigData(message, multipart, messageBodyPart, html);
        } catch (MessagingException ex) {
            System.out.println(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Async
    public void sendEmailForExitingClient(String email, String from, String to, String subject, String senderNameAndSurname) {


        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

            message.setSubject("Tutorio");

            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();
            String html = "";
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/client/existing_client.html");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error reading HTML file: " + e.getMessage());
            }
            html = html.replaceAll("clientName", to);
            html = html.replaceAll("tutorNameAndSurname", senderNameAndSurname);
            setEmailConfigData(message, multipart, messageBodyPart, html);
        } catch (MessagingException ex) {
            System.out.println(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Async
    public void sendEmailForNewClient(String email, String from, String to, String subject, String senderNameAndSurname, String activationLink) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

            message.setSubject("Tutorio");

            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();
            String html = "";
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/client/new_client_register.html");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error reading HTML file: " + e.getMessage());
            }
            html = html.replaceAll("clientName", to);
            html = html.replaceAll("tutorNameAndSurname", senderNameAndSurname);
            html = html.replaceAll("verificationLink", activationLink);
            setEmailConfigData(message, multipart, messageBodyPart, html);
        } catch (MessagingException ex) {
            System.out.println(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendEmailForRegistrationConfirmation(String email, String from, String subject) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

            message.setSubject("Tutorio");

            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();
            String html = "";
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/client/client_confirmed.html");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error reading HTML file: " + e.getMessage());
            }

            setEmailConfigData(message, multipart, messageBodyPart, html);
        } catch (MessagingException ex) {
            System.out.println(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setEmailConfigData(Message message, MimeMultipart multipart, BodyPart messageBodyPart, String html)
        throws MessagingException, IOException {
        messageBodyPart.setContent(html, "text/html; charset=UTF-8");

        multipart.addBodyPart(messageBodyPart);
        messageBodyPart = new MimeBodyPart();
        InputStream imageStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/tutorio-logo.png");
        DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(imageStream, "image/png"));
        messageBodyPart.setDataHandler(dataHandler);

        messageBodyPart.setHeader("Content-ID", "<image>");

        multipart.addBodyPart(messageBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }

    @Async
    public void sendEmailForEventInvitation(String email, String from, String senderNameAndSurname, String date, String clientName) {
        System.out.println("Send email for invivation");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

            message.setSubject("Tutorio");

            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();
            String html = "";
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/client/event_confirmation.html");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error reading HTML file: " + e.getMessage());
            }
            html = html.replaceAll("tutorNameAndSurname", senderNameAndSurname);
            html = html.replaceAll("clientName", clientName);
            html = html.replaceAll("eventDate", date);
            setEmailConfigData(message, multipart, messageBodyPart, html);
        } catch (MessagingException ex) {
            System.out.println(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void sendEmailForEventChange(String email,
                                        String from,
                                        String editor,
                                        String oldDate,
                                        String newDate,
                                        String clientName) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // Get the Session object.
        Session session = Session.getInstance(props,
                                              new javax.mail.Authenticator() {
                                                  protected PasswordAuthentication getPasswordAuthentication() {
                                                      return new PasswordAuthentication(username, password);
                                                  }
                                              });
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

            message.setSubject("Tutorio");

            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();
            String html = "";
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/client/event_change.html");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error reading HTML file: " + e.getMessage());
            }
            html = html.replaceAll("editor", editor);
            html = html.replaceAll("clientName", clientName);
            html = html.replaceAll("oldDate", oldDate);
            html = html.replaceAll("newDate", newDate);
            setEmailConfigData(message, multipart, messageBodyPart, html);
        } catch (MessagingException ex) {
            System.out.println(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendEmailForEventRemoval(String email, String from, String whatHappened1, String meetingSide, String whatHappened2, String date, String clientName) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // Get the Session object.
        Session session = Session.getInstance(props,
                                              new javax.mail.Authenticator() {
                                                  protected PasswordAuthentication getPasswordAuthentication() {
                                                      return new PasswordAuthentication(username, password);
                                                  }
                                              });
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

            message.setSubject("Tutorio");

            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();
            String html = "";
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/client/event_removal.html");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error reading HTML file: " + e.getMessage());
            }
            html = html.replaceAll("whatHappened1", whatHappened1);
            html = html.replaceAll("whatHappened2", whatHappened2);
            html = html.replaceAll("meetingSide", meetingSide);
            html = html.replaceAll("clientName", clientName);
            html = html.replaceAll("eventDate", date);
            setEmailConfigData(message, multipart, messageBodyPart, html);
        } catch (MessagingException ex) {
            System.out.println(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

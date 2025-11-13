package com.tecnoweb.grupo7sa.communication;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class SendEmail {

    // private final String mail = "ginobaptista@gmail.com";
    // private final String password = "dqii suby uhmu arbt";
    private final static String PROTOCOL = "smtp";
    private final String mail = "grupo15sa@tecnoweb.org.bo";
    private final String username = "grupo15sa";
    private final String password = "grup015grup015*";

    public void sendEmail(String to, String response) {
        Properties props = new Properties();
        props.put("mail.transport.protocol", PROTOCOL);
        // props.put("mail.smtp.auth", "true");
        // props.put("mail.smtp.starttls.enable", "true");
        // props.put("mail.smtp.host", "smtp.gmail.com");
        // props.put("mail.smtp.port", "587");

        props.setProperty("mail.smtp.auth", "false");
        // props.put("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.tls.enable", "true");
        props.setProperty("mail.smtp.host", "mail.tecnoweb.org.bo");
        props.setProperty("mail.smtp.port", "25");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Your Subject Here");
            message.setText(response);

            Transport.send(message);

            System.out.println("Correo enviado correctamente");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SendEmail emailSender = new SendEmail();
        emailSender.sendEmail("recipient-email@example.com", "This is the response text");
    }
}
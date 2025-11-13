package com.tecnoweb.grupo15sa.connection;

import com.tecnoweb.grupo15sa.command.CommandInterpreter;
import com.tecnoweb.grupo15sa.communication.MailVerificationThread;
import com.tecnoweb.grupo15sa.communication.SendEmail;
import com.tecnoweb.grupo15sa.interfaces.IEmailEventListener;
import com.tecnoweb.grupo15sa.utils.Email;

import java.util.List;

public class ConnectionCore {
    public SendEmail sendEmail = new SendEmail();

    public static void main(String[] args) {
        MailVerificationThread mail = new MailVerificationThread();
        ConnectionCore core = new ConnectionCore();
        mail.setEmailEventListener(new IEmailEventListener() {

            @Override
            public void onReceiveEmailEvent(List<Email> emails) {
                for (Email email : emails) {
                    System.out.println("Este es el email" + email);
                    String emailFrom = email.getFrom();
                    String emailSubject = email.getSubject();
                    String response = CommandInterpreter.interpret(emailSubject);
                    System.out.println(response);
                    core.sendEmail.sendEmail(emailFrom, response);
                    //System.out.println("Saltando Email por que ta todo lleno");
                }
            }
        });

        Thread thread = new Thread(mail);
        thread.setName("Mail Verification Thread");
        thread.start();
    }
}

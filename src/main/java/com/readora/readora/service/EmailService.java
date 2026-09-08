package com.readora.readora.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendLoginSuccessEmail(
            String recipient,
            String name,
            String role
    ) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(recipient);

            message.setSubject(
                    "READORA - Login Successful"
            );

            message.setText(
                    "Hello " + name + ",\n\n"
                            + "You have successfully logged in to READORA.\n\n"
                            + "Account Role: " + role + "\n\n"
                            + "You can now access your READORA dashboard.\n\n"
                            + "Thank you for using READORA.\n\n"
                            + "READORA Team"
            );

            mailSender.send(message);

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Email could not be sent: "
                            + e.getMessage()
            );

            return false;
        }
    }
}
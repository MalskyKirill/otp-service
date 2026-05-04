package ru.mephi.malskiy.notification;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

public class EmailNotificationServiceImpl implements NotificationService {

    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;

    public EmailNotificationServiceImpl() {
        Properties config = loadConfig();

        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");

        this.session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public void sendCode(String destination, String code) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Email destination is required");
        }

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            message.setSubject("Your OTP Code");
            message.setText("Your verification code is: " + code);

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private Properties loadConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("email.properties not found");
            }

            Properties props = new Properties();
            props.load(inputStream);
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load email configuration", e);
        }
    }
}

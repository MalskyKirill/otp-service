package ru.mephi.malskiy.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class FileNotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FileNotificationServiceImpl.class);
    private final Path file;

    public FileNotificationServiceImpl() {
        this.file = Path.of("otp_codes.txt");
    }

    @Override
    public void sendCode(String destination, String code) {

        String line = String.format(
            "[%s] destination=%s, code=%s%n",
            LocalDateTime.now(),
            destination,
            code
        );

        try {
            Files.writeString(
                file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );

            logger.info("OTP code written to file {}", file.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write OTP code to file", e);
        }
    }
}

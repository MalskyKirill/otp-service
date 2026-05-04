package ru.mephi.malskiy.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TelegramNotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationServiceImpl.class);

    private final String telegramApiUrl;
    private final String chatId;

    public TelegramNotificationServiceImpl() {
        Properties config = loadConfig();

        String token = config.getProperty("telegram.bot.token");
        this.chatId = config.getProperty("telegram.chat.id");

        String urlTemplate = config.getProperty("telegram.api.url");
        this.telegramApiUrl = String.format(urlTemplate, token);
    }

    @Override
    public void sendCode(String destination, String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("OTP code is required");
        }

        String recipientName = (destination == null || destination.isBlank()) ? "User" : destination;

        String message = String.format(
            "%s, your confirmation code is: %s",
            recipientName,
            code
        );

        String url = String.format(
            "%s?chat_id=%s&text=%s",
            telegramApiUrl,
            chatId,
            urlEncode(message)
        );

        sendTelegramRequest(url);
    }

    private void sendTelegramRequest(String url) {
        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            int statusCode = response.statusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Telegram API returned status code " + statusCode + ": " + response.body());
            }

            logger.info("Telegram message sent successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Telegram request interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Properties loadConfig() {
        try (InputStream inputStream = getClass()
            .getClassLoader()
            .getResourceAsStream("telegram.properties")) {

            if (inputStream == null) {
                throw new IllegalStateException("telegram.properties not found");
            }

            Properties props = new Properties();
            props.load(inputStream);
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load telegram configuration", e);
        }
    }
}

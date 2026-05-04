package ru.mephi.malskiy.notification;

import ru.mephi.malskiy.model.enums.NotificationChannel;

public class NotificationServiceFactory {
    private final NotificationService fileNotificationServiceImpl;
    private final NotificationService emailNotificationServiceImpl;
    private final NotificationService smsNotificationServiceImpl;
    private final NotificationService telegramNotificationServiceImpl;

    public NotificationServiceFactory() {
        this.emailNotificationServiceImpl = new EmailNotificationServiceImpl();
        this.fileNotificationServiceImpl = new FileNotificationServiceImpl();
        this.smsNotificationServiceImpl = new SmsNotificationService();
        this.telegramNotificationServiceImpl = new TelegramNotificationServiceImpl();
    }

    public NotificationService getNotificationService(NotificationChannel channel) {
        return switch (channel){
            case FILE -> fileNotificationServiceImpl;
            case EMAIL -> emailNotificationServiceImpl;
            case SMS -> smsNotificationServiceImpl;
            case TELEGRAM -> telegramNotificationServiceImpl;
        };
    }
}

package ru.mephi.malskiy.notification;

import ru.mephi.malskiy.exeption.AppException;
import ru.mephi.malskiy.model.enums.NotificationChannel;

public class NotificationServiceFactory {
    private final NotificationService fileNotificationServiceImpl;
    private final NotificationService emailNotificationServiceImpl;

    public NotificationServiceFactory() {
        this.emailNotificationServiceImpl = new EmailNotificationServiceImpl();
        this.fileNotificationServiceImpl = new FileNotificationServiceImpl();
    }

    public NotificationService getNotificationService(NotificationChannel channel) {
        return switch (channel){
            case FILE -> fileNotificationServiceImpl;
            case EMAIL -> emailNotificationServiceImpl;
            default -> throw new AppException(501, "This channel is not implemented yet");
        };
    }
}

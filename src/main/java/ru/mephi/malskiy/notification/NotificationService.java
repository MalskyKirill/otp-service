package ru.mephi.malskiy.notification;

public interface NotificationService {
    void sendCode(String destination, int code);
}

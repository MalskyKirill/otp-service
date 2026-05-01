package ru.mephi.malskiy.service;

public interface NotificationService {
    void sendCode(String destination, int code);
}

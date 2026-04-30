package ru.mephi.malskiy.model.enums;

public enum NotificationChannel {
    EMAIL,
    SMS,
    TELEGRAM,
    FILE;

    public static NotificationChannel fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Channel is required");
        }

        try {
            return NotificationChannel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid channel: " + value);
        }
    }
}


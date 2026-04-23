package ru.mephi.malskiy.model.enums;

public enum UserRole {
    USER,
    ADMIN;

    public static UserRole fromString(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }

        try {
            return UserRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + value);
        }
    }
}

package ru.mephi.malskiy.dto;

import ru.mephi.malskiy.model.User;
import ru.mephi.malskiy.model.enums.UserRole;

import java.time.LocalDateTime;

public class UserResponseDto {
    private Long id;
    private String login;
    private UserRole role;
    private LocalDateTime createdAt;

    public UserResponseDto() {
    }

    public UserResponseDto(Long id, String login, UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.login = login;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static UserResponseDto from(User user) {
        return new UserResponseDto(user.getId(), user.getLogin(), user.getRole(), user.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

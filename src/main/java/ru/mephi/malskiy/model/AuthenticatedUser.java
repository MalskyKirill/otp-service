package ru.mephi.malskiy.model;

import ru.mephi.malskiy.model.enums.UserRole;

public class AuthenticatedUser {
    private final Long userId;
    private final String login;
    private final UserRole role;

    public AuthenticatedUser(Long userId, String login, UserRole role) {
        this.userId = userId;
        this.login = login;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLogin() {
        return login;
    }

    public UserRole getRole() {
        return role;
    }
}

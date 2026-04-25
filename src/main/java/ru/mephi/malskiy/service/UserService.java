package ru.mephi.malskiy.service;

import ru.mephi.malskiy.dao.UserDao;
import ru.mephi.malskiy.dto.RegisterRequestDto;
import ru.mephi.malskiy.model.User;
import ru.mephi.malskiy.model.enums.UserRole;
import ru.mephi.malskiy.security.PasswordHasher;
import ru.mephi.malskiy.exeption.AppException;

public class UserService {
    private final UserDao userDao;
    private final PasswordHasher passwordHasher;

    public UserService(UserDao userDao, PasswordHasher passwordHasher) {
        this.userDao = userDao;
        this.passwordHasher = passwordHasher;
    }

    public void register(RegisterRequestDto requestDto) {
        validateRegisterRequest(requestDto);

        String login = requestDto.getLogin();
        String password = requestDto.getPassword();
        UserRole role = parseRole(requestDto.getRole());

        if (userDao.existsByLogin(login)) {
            throw new AppException(409, "User with this login already exists");
        }

        if (role == UserRole.ADMIN && userDao.existsAdmin()) {
            throw new AppException(409, "Admin already exists");
        }

        String passwordHash = passwordHasher.hash(password);

        User user = new User(login, passwordHash, role);

        userDao.create(user);
    }

    private void validateRegisterRequest(RegisterRequestDto requestDto) {
        if (requestDto == null) {
            throw new AppException(400, "Request body is required");
        }

        if (requestDto.getLogin() == null || requestDto.getLogin().isBlank()) {
            throw new AppException(400, "Login is required");
        }

        if (requestDto.getPassword() == null || requestDto.getPassword().isBlank()) {
            throw new AppException(400, "Password is required");
        }
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.fromString(role);
        } catch (IllegalArgumentException e) {
            throw new AppException(400, "Role must be ADMIN or USER");
        }
    }
}

package ru.mephi.malskiy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.dao.UserDao;
import ru.mephi.malskiy.dto.AuthResponseDto;
import ru.mephi.malskiy.dto.LoginRequestDto;
import ru.mephi.malskiy.dto.RegisterRequestDto;
import ru.mephi.malskiy.model.User;
import ru.mephi.malskiy.model.enums.UserRole;
import ru.mephi.malskiy.security.JwtService;
import ru.mephi.malskiy.security.PasswordHasher;
import ru.mephi.malskiy.exeption.AppException;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    public UserService(UserDao userDao, PasswordHasher passwordHasher, JwtService jwtService) {
        this.userDao = userDao;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequestDto requestDto) {
        validateRegisterRequest(requestDto);
        logger.info("Register request received: login={}", requestDto.getLogin());

        String login = requestDto.getLogin();
        String password = requestDto.getPassword();
        UserRole role = parseRole(requestDto.getRole());

        if (userDao.existsByLogin(login)) {
            logger.warn("Registration conflict: login already exists: {}", login);
            throw new AppException(409, "User with this login already exists");
        }

        if (role == UserRole.ADMIN && userDao.existsAdmin()) {
            logger.warn("Registration conflict: second admin attempt for login={}", login);
            throw new AppException(409, "Admin already exists");
        }

        String passwordHash = passwordHasher.hash(password);

        User user = new User(login, passwordHash, role);

        userDao.create(user);
        logger.info("User registered successfully: login={}, role={}", login, role);
    }

    public AuthResponseDto login(LoginRequestDto requestDto) {
        validateLoginRequest(requestDto);
        logger.info("Login request received: login={}", requestDto.getLogin());

        String login = requestDto.getLogin().trim();
        String password = requestDto.getPassword().trim();

        User user = userDao.findByLogin(login).orElseThrow(() -> {
            logger.warn("Login failed: user not found for login={}", login);
            return new AppException(401, "Invalid login or password");
        });

        boolean passwordIsValid = passwordHasher.verify(password, user.getPasswordHash());
        if (!passwordIsValid) {
            logger.warn("Login failed: invalid password for login={}", login);
            throw new AppException(401, "Invalid login or password");
        }

        String token = jwtService.generateToken(user);
        logger.info("Login successful: userId={}, login={}", user.getId(), login);

        return new AuthResponseDto(token, "Bearer", jwtService.getExpirationMinutes());
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

    private void validateLoginRequest(LoginRequestDto requestDto) {
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

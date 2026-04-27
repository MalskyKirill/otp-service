package ru.mephi.malskiy.util;

import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import ru.mephi.malskiy.exeption.AppException;
import ru.mephi.malskiy.model.AuthenticatedUser;
import ru.mephi.malskiy.model.enums.UserRole;
import ru.mephi.malskiy.security.JwtService;

public class AuthUtil {

    public static AuthenticatedUser requireAuthentication(HttpExchange exchange, JwtService jwtService) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization"); // берем заголовок

        if (authHeader == null || authHeader.isBlank()) {
            throw new AppException(401, "Authorization header is required");
        }

        if (!authHeader.startsWith("Bearer ")) {
            throw new AppException(401, "Authorization header must start with Bearer");
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new AppException(401, "Token is required");
        }

        try {
            Claims claims = jwtService.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            String login = claims.get("login", String.class);
            String roleValue = claims.get("role", String.class);
            UserRole role = UserRole.valueOf(roleValue);

            return new AuthenticatedUser(userId, login, role);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AppException(401, "Invalid or expired token");
        }
    }

    public static AuthenticatedUser requireAdmin(HttpExchange exchange, JwtService jwtService) {
        AuthenticatedUser user = requireAuthentication(exchange, jwtService);

        if (user.getRole() != UserRole.ADMIN) {
            throw new AppException(403, "Access denied");
        }

        return user;
    }
}

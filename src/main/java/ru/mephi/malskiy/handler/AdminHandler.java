package ru.mephi.malskiy.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.dto.UserResponseDto;
import ru.mephi.malskiy.exeption.AppException;
import ru.mephi.malskiy.security.JwtService;
import ru.mephi.malskiy.service.AdminService;
import ru.mephi.malskiy.service.UserService;
import ru.mephi.malskiy.util.AuthUtil;
import ru.mephi.malskiy.util.ResponseUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(AdminHandler.class);
    private final AdminService adminService;
    private final JwtService jwtService;

    public AdminHandler(AdminService adminService, JwtService jwtService) {
        this.adminService = adminService;
        this.jwtService = jwtService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            AuthUtil.requireAdmin(exchange, jwtService);

            String method = exchange.getRequestMethod();

            if ("get".equalsIgnoreCase(method)) {
                List<UserResponseDto> userResponseDtoList = adminService.getAllNonAdminUsers();
                ResponseUtil.sendJson(exchange, 200, userResponseDtoList);
                return;
            }

            if ("delete".equalsIgnoreCase(method)) {
                delete(exchange);
                return;
            }

            ResponseUtil.sendError(exchange, 405, "Method not allowed");
        }catch (AppException e) {
            ResponseUtil.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Admin OTP config error", e);
            ResponseUtil.sendError(exchange, 500, "Internal server error");
        }
    }

    private void delete(HttpExchange exchange) throws IOException {
        Long userId = extractUserId(exchange);

        adminService.deleteUserById(userId);
        ResponseUtil.sendJson(exchange, 200, Map.of("message", "User deleted successfully"));
    }

    private Long extractUserId(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String contextPath = exchange.getHttpContext().getPath();

        String idPart = path.substring(contextPath.length());

        if (!idPart.matches("/\\d+")) {
            throw new AppException(400, "Valid user id is required in path");
        }

        return Long.parseLong(idPart.substring(1));
    }
}

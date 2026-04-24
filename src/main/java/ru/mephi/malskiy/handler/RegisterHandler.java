package ru.mephi.malskiy.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.dto.RegisterRequestDto;
import ru.mephi.malskiy.exeption.AppException;
import ru.mephi.malskiy.service.UserService;
import ru.mephi.malskiy.util.JsonUtil;
import ru.mephi.malskiy.util.ResponseUtil;

import java.io.IOException;
import java.util.Map;

public class RegisterHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegisterHandler.class);
    private final UserService userService;

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"post".equalsIgnoreCase(exchange.getRequestMethod())) {
            ResponseUtil.sendError(exchange, 405, "Method not allowed");
            return;
        }

        try {
            RegisterRequestDto requestDto = JsonUtil.fromJson(exchange.getRequestBody(), RegisterRequestDto.class);
            userService.register(requestDto);
            ResponseUtil.sendJson(exchange, 201, Map.of("message", "User registered successfully"));
        } catch (AppException ex) {
            ResponseUtil.sendError(exchange, ex.getStatusCode(), ex.getMessage());
        } catch (Exception e) {
            logger.error("Registration error", e);
            ResponseUtil.sendError(exchange, 500, "Internal server error");
        }
    }
}

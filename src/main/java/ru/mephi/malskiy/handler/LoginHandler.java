package ru.mephi.malskiy.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.dto.AuthResponseDto;
import ru.mephi.malskiy.dto.LoginRequestDto;
import ru.mephi.malskiy.exeption.AppException;
import ru.mephi.malskiy.service.UserService;
import ru.mephi.malskiy.util.JsonUtil;
import ru.mephi.malskiy.util.ResponseUtil;

import java.io.IOException;

public class LoginHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    private final UserService userService;

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"post".equalsIgnoreCase(exchange.getRequestMethod())) {
            ResponseUtil.sendError(exchange, 405, "method not allowed");
            return;
        }

        try {
            LoginRequestDto requestDto = JsonUtil.fromJson(exchange.getRequestBody(), LoginRequestDto.class);

            AuthResponseDto responseDto = userService.login(requestDto);

            ResponseUtil.sendJson(exchange, 200, responseDto);
        } catch (AppException e) {
            ResponseUtil.sendError(exchange, e.getStatusCode(), e.getMessage());

        } catch (Exception e) {
            logger.error("Login error", e);
            ResponseUtil.sendError(exchange, 500, "Internal server error");
        }
    }
}

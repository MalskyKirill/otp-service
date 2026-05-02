package ru.mephi.malskiy.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.dto.CreateOtpRequestDto;
import ru.mephi.malskiy.dto.CreateOtpResponseDto;
import ru.mephi.malskiy.exeption.AppException;
import ru.mephi.malskiy.model.AuthenticatedUser;
import ru.mephi.malskiy.security.JwtService;
import ru.mephi.malskiy.service.OtpService;
import ru.mephi.malskiy.util.AuthUtil;
import ru.mephi.malskiy.util.JsonUtil;
import ru.mephi.malskiy.util.ResponseUtil;

import java.io.IOException;

public class UserOtpHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserOtpHandler.class);

    private final OtpService otpService;
    private final JwtService jwtService;

    public UserOtpHandler(OtpService otpService, JwtService jwtService) {
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"post".equalsIgnoreCase(exchange.getRequestMethod())) {
            ResponseUtil.sendError(exchange, 405, "Method not allowed");
            return;
        }

        try {
            AuthenticatedUser user = AuthUtil.requireAuthentication(exchange, jwtService);
            CreateOtpRequestDto request = JsonUtil.fromJson(exchange.getRequestBody(), CreateOtpRequestDto.class);

            CreateOtpResponseDto response = otpService.createOtp(user, request);
            ResponseUtil.sendJson(exchange, 201, response);
        } catch (AppException e) {
            ResponseUtil.sendError(exchange, e.getStatusCode(), e.getMessage());

        } catch (Exception e) {
            logger.error("User OTP create error", e);
            ResponseUtil.sendError(exchange, 500, "Internal server error");
        }
    }
}

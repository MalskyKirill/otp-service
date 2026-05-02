package ru.mephi.malskiy.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.dto.ValidOtpResponseDto;
import ru.mephi.malskiy.dto.ValidateOtpRequestDto;
import ru.mephi.malskiy.exeption.AppException;
import ru.mephi.malskiy.model.AuthenticatedUser;
import ru.mephi.malskiy.security.JwtService;
import ru.mephi.malskiy.service.OtpService;
import ru.mephi.malskiy.util.AuthUtil;
import ru.mephi.malskiy.util.JsonUtil;
import ru.mephi.malskiy.util.ResponseUtil;

import java.io.IOException;

public class UserOtpValidationHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserOtpValidationHandler.class);
    private final OtpService otpService;
    private final JwtService jwtService;

    public UserOtpValidationHandler(OtpService otpService, JwtService jwtService) {
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

            ValidateOtpRequestDto request = JsonUtil.fromJson(exchange.getRequestBody(), ValidateOtpRequestDto.class);

            ValidOtpResponseDto response = otpService.validateOtp(user, request);
            ResponseUtil.sendJson(exchange, 200, response);
        } catch (AppException e) {
            ResponseUtil.sendError(exchange, e.getStatusCode(), e.getMessage());

        } catch (Exception e) {
            logger.error("User OTP validation error", e);
            ResponseUtil.sendError(exchange, 500, "Internal server error");
        }


    }
}

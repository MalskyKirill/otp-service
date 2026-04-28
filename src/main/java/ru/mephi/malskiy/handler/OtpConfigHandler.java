package ru.mephi.malskiy.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.dto.UpdateOtpConfigRequestDto;
import ru.mephi.malskiy.exeption.AppException;
import ru.mephi.malskiy.model.OtpConfig;
import ru.mephi.malskiy.security.JwtService;
import ru.mephi.malskiy.service.AdminService;
import ru.mephi.malskiy.util.AuthUtil;
import ru.mephi.malskiy.util.JsonUtil;
import ru.mephi.malskiy.util.ResponseUtil;

import java.io.IOException;

public class OtpConfigHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(OtpConfigHandler.class);

    private final AdminService adminService;
    private final JwtService jwtService;

    public OtpConfigHandler(AdminService adminService, JwtService jwtService) {
        this.adminService = adminService;
        this.jwtService = jwtService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            AuthUtil.requireAdmin(exchange, jwtService);

            String method = exchange.getRequestMethod();

            if ("get".equalsIgnoreCase(method)) {
                OtpConfig otpConfig = adminService.getOtpConfig();
                ResponseUtil.sendJson(exchange, 200, otpConfig);
                return;
            }

            if ("put".equalsIgnoreCase(method)) {
                UpdateOtpConfigRequestDto request = JsonUtil.fromJson(exchange.getRequestBody(),
                    UpdateOtpConfigRequestDto.class);

                OtpConfig otpConfig = adminService.updateOtpConfig(request);

                ResponseUtil.sendJson(exchange, 201, otpConfig);

                return;
            }

            ResponseUtil.sendError(exchange, 405, "Method not allowed");
        } catch (AppException e) {
            ResponseUtil.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Admin OTP config error", e);
            ResponseUtil.sendError(exchange, 500, "Internal server error");
        }
    }
}

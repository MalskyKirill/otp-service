package ru.mephi.malskiy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.dao.OtpCodeDao;
import ru.mephi.malskiy.dao.OtpConfigDao;
import ru.mephi.malskiy.dto.CreateOtpRequestDto;
import ru.mephi.malskiy.dto.CreateOtpResponseDto;
import ru.mephi.malskiy.dto.ValidOtpResponseDto;
import ru.mephi.malskiy.dto.ValidateOtpRequestDto;
import ru.mephi.malskiy.exeption.AppException;
import ru.mephi.malskiy.model.AuthenticatedUser;
import ru.mephi.malskiy.model.OtpCode;
import ru.mephi.malskiy.model.OtpConfig;
import ru.mephi.malskiy.model.enums.NotificationChannel;
import ru.mephi.malskiy.notification.NotificationService;
import ru.mephi.malskiy.notification.NotificationServiceFactory;
import ru.mephi.malskiy.util.OtpGenerator;

import java.time.LocalDateTime;

public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final OtpGenerator otpGenerator;
    private final NotificationServiceFactory notificationServiceFactory;

    public OtpService(OtpCodeDao otpCodeDao, OtpConfigDao otpConfigDao, OtpGenerator otpGenerator, NotificationServiceFactory notificationServiceFactory) {
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
        this.otpGenerator = otpGenerator;
        this.notificationServiceFactory = notificationServiceFactory;
    }

    public CreateOtpResponseDto createOtp(AuthenticatedUser user, CreateOtpRequestDto request) {
        if (user == null) {
            throw new AppException(401, "Unauthorized");
        }

        validateCreateRequest(request);

        NotificationChannel channel = parseChanel(request.getChanel());
        logger.info("Create OTP request: userId={}, operationId={}, channel={}", user.getUserId(), request.getOperationId(), channel);

        OtpConfig otpConfig = otpConfigDao.getOtpConfig()
            .orElseThrow(() -> new AppException(404, "OTP config not found"));

        NotificationService notificationService = notificationServiceFactory.getNotificationService(channel);

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expiresAt = now.plusSeconds(otpConfig.getLifetimeSeconds());

        String code = otpGenerator.generateCode(otpConfig.getCodeLength());

        otpCodeDao.expireActiveCodesForUserAndOperation(user.getUserId(), request.getOperationId());
        otpCodeDao.create(user.getUserId(), request.getOperationId(), code, expiresAt);
        logger.info("OTP created: userId={}, operationId={}, expiresAt={}", user.getUserId(), request.getOperationId(), expiresAt);

        notificationService.sendCode(request.getDestination(), code);
        logger.info("OTP notification sent: userId={}, operationId={}, destination={}", user.getUserId(), request.getOperationId(), request.getDestination());

        return new CreateOtpResponseDto(
            "OTP code generated successfully",
            request.getOperationId(),
            channel.name(),
            expiresAt);
    }

    public ValidOtpResponseDto validateOtp(AuthenticatedUser user, ValidateOtpRequestDto request) {
        if (user == null) {
            throw new AppException(401, "Unauthorized");
        }

        validateValidRequest(request);
        logger.info("Validate OTP request: userId={}, operationId={}", user.getUserId(), request.getOperationId());

        OtpCode otpCode = otpCodeDao.findLatestActiveByUserAndOperation(user.getUserId(), request.getOperationId())
            .orElseThrow(() -> new AppException(404, "Active OTP code not found"));

        LocalDateTime now = LocalDateTime.now();

        if (!now.isBefore(otpCode.getExpiresAt())) {
            otpCodeDao.marcAsExpired(otpCode.getId());
            logger.info("OTP expired during validation: userId={}, operationId={}, otpId={}", user.getUserId(), request.getOperationId(), otpCode.getId());
            return new ValidOtpResponseDto(false, "EXPIRED", "Otp code expired");
        }

        if (!otpCode.getCode().equals(request.getCode())) {
            logger.info("OTP code mismatch: userId={}, operationId={}, otpId={}", user.getUserId(), request.getOperationId(), otpCode.getId());
            return new ValidOtpResponseDto(false, "ACTIVE", "Invalid OTP code");
        }

        otpCodeDao.marcAsUsed(otpCode.getId(), now);
        logger.info("OTP validated successfully: userId={}, operationId={}, otpId={}", user.getUserId(), request.getOperationId(), otpCode.getId());
        return new ValidOtpResponseDto(true, "USED", "OTP validated successfully");
    }

    private void validateCreateRequest(CreateOtpRequestDto request) {
        if (request == null) {
            throw new AppException(400, "Request is required");
        }

        if (request.getOperationId() == null || request.getOperationId().isBlank()) {
            throw new AppException(400, "Operation is required");
        }

        if (request.getChanel() == null || request.getChanel().isBlank()) {
            throw new AppException(400, "Chanel is required");
        }
    }

    private void validateValidRequest(ValidateOtpRequestDto request) {
        if (request == null) {
            throw new AppException(400, "Request is required");
        }

        if (request.getOperationId() == null || request.getOperationId().isBlank()) {
            throw new AppException(400, "Operation is required");
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new AppException(400, "Code is required");
        }
    }

    private NotificationChannel parseChanel(String chanel) {
        try {
            return NotificationChannel.fromString(chanel);
        } catch (IllegalArgumentException e) {
            throw new AppException(400, "Invalid chanel");
        }

    }
}

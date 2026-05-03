package ru.mephi.malskiy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.dao.OtpConfigDao;
import ru.mephi.malskiy.dao.UserDao;
import ru.mephi.malskiy.dto.UpdateOtpConfigRequestDto;
import ru.mephi.malskiy.dto.UserResponseDto;
import ru.mephi.malskiy.exeption.AppException;
import ru.mephi.malskiy.model.OtpConfig;
import ru.mephi.malskiy.model.User;
import ru.mephi.malskiy.model.enums.UserRole;

import java.util.List;

public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final UserDao userDao;
    private final OtpConfigDao otpConfigDao;

    public AdminService(UserDao userDao, OtpConfigDao otpConfigDao) {
        this.userDao = userDao;
        this.otpConfigDao = otpConfigDao;
    }

    public OtpConfig getOtpConfig() {
        logger.info("Admin requested OTP config");
        return otpConfigDao.getOtpConfig().orElseThrow(() -> new AppException(500, "OTP config not found"));
    }

    public OtpConfig updateOtpConfig(UpdateOtpConfigRequestDto request) {
        logger.info("Admin update OTP config request received");
        if (request == null) {
            throw new AppException(400, "Request body is required");
        }

        if (request.getCodeLength() == null) {
            throw new AppException(400, "codeLength is required");
        }

        if (request.getLifetimeSeconds() == null) {
            throw new AppException(400, "lifetimeSeconds is required");
        }

        int codeLength = request.getCodeLength();
        int lifetimeSeconds = request.getLifetimeSeconds();

        if (codeLength < 4 || codeLength > 10) {
            throw new AppException(400, "codeLength must be between 4 and 10");
        }

        if (lifetimeSeconds < 30 || lifetimeSeconds > 3600) {
            throw new AppException(400, "lifetimeSeconds must be between 30 and 3600");
        }

        OtpConfig updated = otpConfigDao.updateOptConfig(codeLength, lifetimeSeconds);
        logger.info("OTP config updated: codeLength={}, lifetimeSeconds={}", codeLength, lifetimeSeconds);
        return updated;
    }

    public List<UserResponseDto> getAllNonAdminUsers() {
        logger.info("Admin requested all non-admin users");
        return userDao.findAllUsersExpectAdmins().stream().map(UserResponseDto::from).toList();
    }

    public void deleteUserById(Long id) {
        logger.info("Admin delete user request: userId={}", id);
        if (id == null) {
            throw new AppException(400, "User id is required");
        }

        User user = userDao.findUserById(id).orElseThrow(() -> new AppException(404, "User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            logger.warn("Admin delete denied for admin userId={}", id);
            throw new AppException(403, "Admin user cannot be deleted");
        }

        userDao.deleteUserById(id);
        logger.info("User deleted by admin: userId={}", id);
    }
}

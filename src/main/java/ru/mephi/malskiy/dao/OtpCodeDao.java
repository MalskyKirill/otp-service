package ru.mephi.malskiy.dao;

import ru.mephi.malskiy.config.Database;
import ru.mephi.malskiy.dto.CreateOtpResponseDto;
import ru.mephi.malskiy.model.OtpCode;
import ru.mephi.malskiy.model.enums.OtpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class OtpCodeDao {
    private final Database database;

    public OtpCodeDao(Database database) {
        this.database = database;
    }

    public OtpCode create(Long userId, String operationId, String code, LocalDateTime expiresAt) {
        String sql = """
            INSERT INTO otp_code (user_id, operation_id, code, status, expires_at)
            VALUES (?, ?, ?, 'ACTIVE', ?)
            RETURNING id, user_id, operation_id, code, status, created_at, expires_at, used_at
            """;

        try(Connection connection = database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, operationId);
            preparedStatement.setString(3, code);
            preparedStatement.setObject(4, expiresAt);

            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapOtpCode(resultSet);
                }
            }

            throw new SQLException("Failed to create otp code");
        } catch (SQLException e) {
            throw new RuntimeException("Database error while creating otp code", e);
        }
    }

    public Optional<OtpCode> findLatestActiveByUserAndOperation(Long userId, String operationId) {
        String sql = """
            SELECT id, user_id, operation_id, code, status, created_at, expires_at, used_at
            FROM otp_code
            WHERE user_id = ?
            AND operation_id = ?
            AND status = 'ACTIVE'
            ORDER BY id DESC
            LIMIT 1
            """;

        try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, operationId);

            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    return Optional.of(mapOtpCode(resultSet));
                }

            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Database error while reading otp code", e);
        }
    }

    public void expireActiveCodesForUserAndOperation(Long userId, String operationId) {
        String sql = """
            UPDATE otp_codes
            SET status 'EXPIRED'
            WHERE user_id = ?
            AND operation_id = ?
            AND status = 'ACTIVE'
            """;

        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, operationId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Database error while expiring previous otp codes", e);
        }
    }

    public void marcAsUsed(Long optId, LocalDateTime usedAt) {
        String sql = """
            UPDATE otp_codes
            SET status = 'USED', used_at = ?
            WHERE id = ?
            """;

        try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, usedAt);
            preparedStatement.setLong(2, optId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error while marking otp code as used", e);
        }
    }

    public void marcAsExpired(Long optId) {
        String sql = """
            UPDATE otp_codes
            SET status = 'EXPIRED'
            WHERE id = ?
            """;

        try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, optId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error while marking otp code as expired", e);
        }
    }

    public int marcExpiredCodes(LocalDateTime now) {
        String sql = """
            UPDATE otp_codes
            SET status = 'EXPIRED'
            WHERE status = 'ACTIVE'
            AND expires_at <= ?
            """;

        try (Connection connection = database.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            preparedStatement.setObject(1, now);
            return preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private OtpCode mapOtpCode(ResultSet resultSet) throws SQLException {
        return new OtpCode(
            resultSet.getLong("id"),
            resultSet.getLong("user_id"),
            resultSet.getString("operation_id"),
            resultSet.getString("code"),
            OtpStatus.valueOf(resultSet.getString("status")),
            resultSet.getObject("created_at", LocalDateTime.class),
            resultSet.getObject("expires_at", LocalDateTime.class),
            resultSet.getObject("used_at", LocalDateTime.class)
        );
    }
}

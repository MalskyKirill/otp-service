package ru.mephi.malskiy.dao;

import ru.mephi.malskiy.config.Database;
import ru.mephi.malskiy.model.OtpConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class OtpConfigDao {
    private final Database database;

    public OtpConfigDao(Database database) {
        this.database = database;
    }

    public Optional<OtpConfig> getOtpConfig() {
        String sql = """
            SELECT id, code_length, lifetime_seconds
            FROM otp_config
            WHERE id = 1
            """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return Optional.of(mapConfig(resultSet));
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Database error while reading otp config", e);
        }
    }

    public Optional<OtpConfig> updateOptConfig(int codeLength, int lifetimeSeconds) {
        String sql = """
            UPDATE opt_config
            SET code_length = ?, lifetime_seconds = ?
            WHERE id = 1
            RETURNING id, code_length, lifetime_seconds
            """;

        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            preparedStatement.setInt(1, codeLength);
            preparedStatement.setInt(2, lifetimeSeconds);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapConfig(resultSet));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Database error while updating otp config", e);
        }
    }

    private OtpConfig mapConfig(ResultSet resultSet) throws SQLException {
        return new OtpConfig(resultSet.getLong("id"),
            resultSet.getInt("code_length"),
            resultSet.getInt("lifetime_seconds"));
    }

}

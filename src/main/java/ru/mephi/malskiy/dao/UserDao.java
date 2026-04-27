package ru.mephi.malskiy.dao;

import ru.mephi.malskiy.config.Database;
import ru.mephi.malskiy.model.User;
import ru.mephi.malskiy.model.enums.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserDao {

    private final Database database;

    public UserDao(Database database) {
        this.database = database;
    }

    public User create(User user) {
        String sql = """
            INSERT INTO users (login, password_hash, role)
            VALUES (?, ?, ?)
            RETURNING id, login, password_hash, role, created_at
            """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole().name());

            try (ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
            throw new SQLException("Failed to create user");

        } catch (SQLException e) {
            throw new RuntimeException("Database error while creating user", e);
        }
    }

    public Optional<User> findByLogin(String login) {
        String sql = """
            SELECT id, login, password_hash, role, created_at
            FROM users
            WHERE login = ?
            """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Database error while finding user by login", e);
        }
    }

    public boolean existsByLogin(String login) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE login = ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()){
                resultSet.next();
                return resultSet.getBoolean(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error while checking user login", e);
        }
    }

    public boolean existsAdmin() {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE role = 'ADMIN')";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("Database error while checking admin existence", e);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        String login = resultSet.getString("login");
        String passwordHash = resultSet.getString("password_hash");
        UserRole role = UserRole.valueOf(resultSet.getString("role"));
        LocalDateTime createdAt = resultSet.getObject("created_at", LocalDateTime.class);

        return new User(id, login, passwordHash, role, createdAt);
    }
}

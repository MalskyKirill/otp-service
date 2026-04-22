package ru.mephi.malskiy;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.config.Database;
import ru.mephi.malskiy.config.DatabaseConfig;
import ru.mephi.malskiy.handler.HealthHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;

public class OtpApp {
    private static final Logger logger = LoggerFactory.getLogger(OtpApp.class);

    public static void main(String[] args) throws IOException {
        DatabaseConfig config = new DatabaseConfig();
        Database database = new Database(config);

        checkDatabaseConnection(database);

        HttpServer server = HttpServer.create(new InetSocketAddress(config.getServerPort()), 0);

        server.createContext("/health", new HealthHandler());

        server.setExecutor(null);
        server.start();
        logger.info("OTP service started on port {}", config.getServerPort());
    }

    private static void checkDatabaseConnection(Database database) {
        try (Connection connection = database.getConnection()) {
            logger.info("Database connection successful");
        } catch (SQLException e) {
            logger.error("Database connection failed", e);
            throw new RuntimeException("Cannot connect to database", e);
        }
    }
}

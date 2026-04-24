package ru.mephi.malskiy;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.config.AppConfig;
import ru.mephi.malskiy.config.Database;
import ru.mephi.malskiy.config.SchemaInitializer;
import ru.mephi.malskiy.dao.UserDao;
import ru.mephi.malskiy.handler.HealthHandler;
import ru.mephi.malskiy.handler.RegisterHandler;
import ru.mephi.malskiy.security.PasswordHasher;
import ru.mephi.malskiy.service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;

public class OtpApp {
    private static final Logger logger = LoggerFactory.getLogger(OtpApp.class);

    public static void main(String[] args) throws IOException {
        AppConfig config = new AppConfig();
        Database database = new Database(config);
        SchemaInitializer schemaInitializer = new SchemaInitializer(database);
        schemaInitializer.init();

        checkDatabaseConnection(database);

        UserDao userDao = new UserDao(database);
        PasswordHasher passwordHasher = new PasswordHasher();
        UserService userService = new UserService(userDao, passwordHasher);

        HttpServer server = HttpServer.create(new InetSocketAddress(config.getServerPort()), 0);

        server.createContext("/health", new HealthHandler());
        server.createContext("/user/register", new RegisterHandler(userService));

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

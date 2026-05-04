package ru.mephi.malskiy;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mephi.malskiy.config.AppConfig;
import ru.mephi.malskiy.config.Database;
import ru.mephi.malskiy.config.SchemaInitializer;
import ru.mephi.malskiy.dao.OtpCodeDao;
import ru.mephi.malskiy.dao.OtpConfigDao;
import ru.mephi.malskiy.dao.UserDao;
import ru.mephi.malskiy.handler.*;
import ru.mephi.malskiy.notification.NotificationServiceFactory;
import ru.mephi.malskiy.scheduler.OtpExpirationScheduler;
import ru.mephi.malskiy.security.JwtService;
import ru.mephi.malskiy.security.PasswordHasher;
import ru.mephi.malskiy.service.AdminService;
import ru.mephi.malskiy.service.OtpService;
import ru.mephi.malskiy.service.UserService;
import ru.mephi.malskiy.util.OtpGenerator;

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
        OtpConfigDao otpConfigDao = new OtpConfigDao(database);
        OtpCodeDao otpCodeDao = new OtpCodeDao(database);

        PasswordHasher passwordHasher = new PasswordHasher();
        JwtService jwtService = new JwtService(config);

        UserService userService = new UserService(userDao, passwordHasher, jwtService);
        AdminService adminService = new AdminService(userDao, otpConfigDao);
        OtpService otpService = new OtpService(
            otpCodeDao,
            otpConfigDao,
            new OtpGenerator(),
            new NotificationServiceFactory());

        OtpExpirationScheduler scheduler = new OtpExpirationScheduler(otpCodeDao);
        scheduler.start();

        HttpServer server = HttpServer.create(new InetSocketAddress(config.getServerPort()), 0);

        server.createContext("/health", new HealthHandler());
        server.createContext("/auth/register", new RegisterHandler(userService));
        server.createContext("/auth/login", new LoginHandler(userService));

        server.createContext("/admin/otp-config", new OtpConfigHandler(adminService, jwtService));
        server.createContext("/admin/users", new AdminHandler(adminService, jwtService));

        server.createContext("/user/otp", new UserOtpHandler(otpService, jwtService));
        server.createContext("/user/otp/validate", new UserOtpValidationHandler(otpService, jwtService));

        server.setExecutor(null);
        server.start();
        logger.info("OTP service started on port {}", config.getServerPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down OTP service");
            scheduler.stop();
        }));
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

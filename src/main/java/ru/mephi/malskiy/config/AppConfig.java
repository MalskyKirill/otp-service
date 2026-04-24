package ru.mephi.malskiy.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final Properties properties = new Properties();

    public AppConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {

            if (inputStream == null) {
                throw new IllegalArgumentException("application.properties not found");
            }

            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    public String getDBUrl() {
        return properties.getProperty("db.url");
    }

    public String getDbUsername() {
        return properties.getProperty("db.username");
    }

    public String getDbPassword() {
        return properties.getProperty("db.password");
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port"));
    }
}

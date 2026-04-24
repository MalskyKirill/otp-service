package ru.mephi.malskiy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {
    private static final Logger logger = LoggerFactory.getLogger(SchemaInitializer.class);

    private final Database database;

    public SchemaInitializer(Database database) {
        this.database = database;
    }

    public void init() {
        try(Connection connection = database.getConnection()) {
            executeSchema(connection);
            logger.info("Database schema initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database schema", e);
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    private void executeSchema(Connection connection) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema.sql");
        if (inputStream == null) {
            throw new IllegalStateException("schema.sql not found in resources");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             Statement statement= connection.createStatement()) {
            StringBuilder sql = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (trimmed.isEmpty()) {
                    continue;
                }

                sql.append(line).append('\n');

                if (trimmed.endsWith(";")) {
                    String query = sql.toString().trim();
                    logger.debug("Executing SQL: {}", query);
                    statement.execute(query);
                    sql.setLength(0);
                }
            }
        }
    }
}

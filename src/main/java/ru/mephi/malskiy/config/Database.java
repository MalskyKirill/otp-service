package ru.mephi.malskiy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private final DatabaseConfig databaseConfig;

    public Database(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseConfig.getDBUrl(), databaseConfig.getDbUsername(), databaseConfig.getDbPassword());
    }
}

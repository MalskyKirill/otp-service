package ru.mephi.malskiy.dao;

import ru.mephi.malskiy.config.Database;

public class UserDao {

    private final Database database;

    public UserDao(Database database) {
        this.database = database;
    }
}

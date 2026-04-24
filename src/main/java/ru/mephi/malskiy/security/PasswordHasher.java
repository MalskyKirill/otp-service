package ru.mephi.malskiy.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean verify(String password, String passwordHash) {
        return BCrypt.checkpw(password, passwordHash);
    }
}

package ru.mephi.malskiy.util;

import java.security.SecureRandom;

public class OtpGenerator {
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be positive");
        }

        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(secureRandom.nextInt(10));
        }

        return code.toString();
    }
}

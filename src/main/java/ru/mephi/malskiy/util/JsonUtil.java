package ru.mephi.malskiy.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    public static <T> T fromJson(InputStream inputStream, Class<T> cl) {
        try {
            return objectMapper.readValue(inputStream, cl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
}

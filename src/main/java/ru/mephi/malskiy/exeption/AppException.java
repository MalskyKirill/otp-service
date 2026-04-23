package ru.mephi.malskiy.exeption;

public class AppException extends RuntimeException{
    private final int StatusCode;

    public AppException(int statusCode, String message) {
        super(message);
        StatusCode = statusCode;
    }

    public int getStatusCode() {
        return StatusCode;
    }
}

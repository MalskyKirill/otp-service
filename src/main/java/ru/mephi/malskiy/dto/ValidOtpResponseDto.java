package ru.mephi.malskiy.dto;

public class ValidOtpResponseDto {
    private boolean valid;
    private String status;
    private String message;

    public ValidOtpResponseDto(boolean valid, String status, String message) {
        this.valid = valid;
        this.status = status;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String isStatus() {
        return status;
    }

    public String isMessage() {
        return message;
    }
}

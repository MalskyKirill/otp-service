package ru.mephi.malskiy.dto;

public class ValidOtpResponseDto {
    private boolean valid;
    private boolean status;
    private boolean message;

    public ValidOtpResponseDto(boolean valid, boolean status, boolean message) {
        this.valid = valid;
        this.status = status;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isStatus() {
        return status;
    }

    public boolean isMessage() {
        return message;
    }
}

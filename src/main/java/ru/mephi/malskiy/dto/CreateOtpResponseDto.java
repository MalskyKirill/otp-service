package ru.mephi.malskiy.dto;

import java.time.LocalDateTime;

public class CreateOtpResponseDto {
    private String message;
    private String operationId;
    private String channel;
    private LocalDateTime expiresAt;

    public CreateOtpResponseDto() {
    }

    public CreateOtpResponseDto(String message, String operationId, String channel, LocalDateTime expiresAt) {
        this.message = message;
        this.operationId = operationId;
        this.channel = channel;
        this.expiresAt = expiresAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}

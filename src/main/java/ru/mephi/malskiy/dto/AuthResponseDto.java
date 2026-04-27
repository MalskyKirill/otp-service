package ru.mephi.malskiy.dto;

public class AuthResponseDto {
    private String token;
    private String type;
    private long expiresInMinutes;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token, String type, long expiresInMinutes) {
        this.token = token;
        this.type = type;
        this.expiresInMinutes = expiresInMinutes;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getExpiresInMinutes() {
        return expiresInMinutes;
    }

    public void setExpiresInMinutes(int expiresInMinutes) {
        this.expiresInMinutes = expiresInMinutes;
    }
}

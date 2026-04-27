package ru.mephi.malskiy.dto;

public class UpdateOtpConfigRequestDto {
    private Integer codeLength;
    private Integer lifetimeSeconds;

    public Integer getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(Integer codeLength) {
        this.codeLength = codeLength;
    }

    public Integer getLifetimeSeconds() {
        return lifetimeSeconds;
    }

    public void setLifetimeSeconds(Integer lifetimeSeconds) {
        this.lifetimeSeconds = lifetimeSeconds;
    }
}

package ru.mephi.malskiy.model;

public class OtpConfig {
    private Long id;
    private int codeLength;
    private int lifetimeSeconds;

    public OtpConfig() {
    }

    public OtpConfig(Long id, int codeLength, int lifetimeSeconds) {
        this.id = id;
        this.codeLength = codeLength;
        this.lifetimeSeconds = lifetimeSeconds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public int getLifetimeSeconds() {
        return lifetimeSeconds;
    }

    public void setLifetimeSeconds(int lifetimeSeconds) {
        this.lifetimeSeconds = lifetimeSeconds;
    }
}

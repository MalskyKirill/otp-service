package ru.mephi.malskiy.dto;

public class ValidateOtpRequestDto {
    private String operationId;
    private String code;

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

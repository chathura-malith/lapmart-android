package com.metkring.lapmart.dto;

public class StandardResponseDto {
    private int code;
    private String message;
    private Object data;

    public StandardResponseDto() {
    }

    public StandardResponseDto(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}

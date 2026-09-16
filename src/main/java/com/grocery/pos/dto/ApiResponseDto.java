package com.grocery.pos.dto;

public class ApiResponseDto {
    private boolean success;
    private String message;
    private Object data;

    public ApiResponseDto() {
    }

    public ApiResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponseDto(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ApiResponseDto ok(String message, Object data) {
        return new ApiResponseDto(true, message, data);
    }

    public static ApiResponseDto ok(String message) {
        return new ApiResponseDto(true, message, null);
    }

    public static ApiResponseDto error(String message) {
        return new ApiResponseDto(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

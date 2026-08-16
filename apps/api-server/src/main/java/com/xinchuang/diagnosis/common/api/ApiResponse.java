package com.xinchuang.diagnosis.common.api;

import java.time.Instant;
import java.util.UUID;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private String traceId;
    private Instant timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return of(200, "success", data);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return of(code, message, null);
    }

    public static <T> ApiResponse<T> of(int code, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        response.setTraceId(UUID.randomUUID().toString());
        response.setTimestamp(Instant.now());
        return response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}


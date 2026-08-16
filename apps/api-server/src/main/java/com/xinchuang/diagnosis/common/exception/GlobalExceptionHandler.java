package com.xinchuang.diagnosis.common.exception;

import com.xinchuang.diagnosis.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        HttpStatus status = HttpStatus.OK;
        if (ex.getCode() == 401) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex.getCode() == 403) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex.getCode() == 503) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return ResponseEntity.status(status).body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        BindException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception ex) {
        String message = "请求参数不合法";
        if (ex instanceof MethodArgumentNotValidException validationException) {
            FieldError fieldError = validationException.getBindingResult().getFieldError();
            if (fieldError != null && fieldError.getDefaultMessage() != null) {
                message = fieldError.getDefaultMessage();
            }
        } else if (ex instanceof BindException bindException) {
            FieldError fieldError = bindException.getBindingResult().getFieldError();
            if (fieldError != null && fieldError.getDefaultMessage() != null) {
                message = fieldError.getDefaultMessage();
            }
        } else if (ex instanceof ConstraintViolationException violationException && !violationException.getConstraintViolations().isEmpty()) {
            message = violationException.getConstraintViolations().iterator().next().getMessage();
        } else if (ex instanceof HttpMessageNotReadableException) {
            message = "请求内容格式不合法";
        }
        return ResponseEntity.ok(ApiResponse.fail(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        LOGGER.error("Unhandled server exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(500, "系统繁忙，请稍后重试"));
    }
}

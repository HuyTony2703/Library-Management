package com.library.backend.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class CatalogValidationException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;
    private final Map<String, String> fieldErrors;
    private final Map<String, Object> details;

    public CatalogValidationException(
            HttpStatus status,
            String errorCode,
            String message,
            Map<String, String> fieldErrors,
            Map<String, Object> details
    ) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
        this.details = details;
    }

    public HttpStatus getStatus() { return status; }
    public String getErrorCode() { return errorCode; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public Map<String, Object> getDetails() { return details; }
}

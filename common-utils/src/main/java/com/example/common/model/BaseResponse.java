package com.example.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"statusCode", "message", "error", "data"})
public class BaseResponse<T> {

    private final HttpStatusCode statusCode;
    private final String message;
    private final boolean error;
    private final T data;

    public BaseResponse(T data) {
        this.statusCode = HttpStatus.OK;
        this.message = null;
        this.error = false;
        this.data = data;
    }

    public BaseResponse(String message, boolean error, HttpStatusCode statusCode) {
        this.statusCode = statusCode;
        this.message = message;
        this.error = error;
        this.data = null;
    }

    public BaseResponse(T data, String message, boolean error, HttpStatusCode statusCode) {
        this.statusCode = statusCode;
        this.message = message;
        this.error = error;
        this.data = data;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return error;
    }

    public T getData() {
        return data;
    }
}

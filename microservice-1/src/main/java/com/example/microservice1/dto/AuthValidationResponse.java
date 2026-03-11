package com.example.microservice1.dto;

public class AuthValidationResponse {

    private String username;
    private boolean valid;
    private String message;

    public AuthValidationResponse() {
    }

    public AuthValidationResponse(String username, boolean valid, String message) {
        this.username = username;
        this.valid = valid;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package com.fooddelivery.authservice.dto;

public class ValidationResponse {
    private boolean valid;
    private Long userId;
    private String email;
    private String role;
    private String message;

    public ValidationResponse(boolean valid, Long userId, String email, String role) {
        this.valid = valid;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public ValidationResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    // Getters and Setters
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
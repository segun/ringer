package com.idempotent.ringer.ui.data;

public class UserResponse {
    private String message;
    private UserDetails user;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }
}

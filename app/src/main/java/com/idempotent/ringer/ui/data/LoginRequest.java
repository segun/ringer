package com.idempotent.ringer.ui.data;

public class LoginRequest {
    private String emailOrPhone;
    private String password;
    private String location;
    private boolean manualLocation;

    public LoginRequest(String emailOrPhone, String password, String userLocation, boolean manualLocation) {
        this.emailOrPhone = emailOrPhone;
        this.password = password;
        this.location = userLocation;
        this.manualLocation = manualLocation;
    }

    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public void setEmailOrPhone(String emailOrPhone) {
        this.emailOrPhone = emailOrPhone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isManualLocation() {
        return manualLocation;
    }

    public void setManualLocation(boolean manualLocation) {
        this.manualLocation = manualLocation;
    }
}
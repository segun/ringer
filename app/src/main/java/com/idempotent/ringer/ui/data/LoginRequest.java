package com.idempotent.ringer.ui.data;

public class LoginRequest {
    private String emailOrPhone;
    private String passcode;
    private String location;
    private boolean manualLocation;

    public LoginRequest(String emailOrPhone, String passcode, String userLocation, boolean manualLocation) {
        this.emailOrPhone = emailOrPhone;
        this.passcode = passcode;
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

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public boolean isManualLocation() {
        return manualLocation;
    }

    public void setManualLocation(boolean manualLocation) {
        this.manualLocation = manualLocation;
    }
}
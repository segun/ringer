package com.idempotent.ringer.ui.data;

public class RegisterRequest {
    private String emailOrPhone;
    private String passcode;
    private String location;
    private boolean manualLocation;

    public RegisterRequest(String emailOrPhone, String passcode, String location, boolean manualLocation) {
        this.emailOrPhone = emailOrPhone;
        this.location = location;
        this.manualLocation = manualLocation;
        this.passcode = passcode;
    }

    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public void setEmailOrPhone(String emailOrPhone) {
        this.emailOrPhone = emailOrPhone;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isManualLocation() {
        return manualLocation;
    }

    public void setManualLocation(boolean manualLocation) {
        this.manualLocation = manualLocation;
    }
}
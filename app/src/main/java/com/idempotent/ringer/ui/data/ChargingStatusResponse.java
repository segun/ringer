package com.idempotent.ringer.ui.data;

public class ChargingStatusResponse {
    private long statusTime;
    private boolean manualLocation;
    private String userId;
    private String id;
    private boolean isPluggedIn;
    private String userLocation;

    // Default constructor
    public ChargingStatusResponse() {
    }

    // Parameterized constructor
    public ChargingStatusResponse(long statusTime, boolean manualLocation, String userId,
                                  String id, boolean isPluggedIn, String userLocation) {
        this.statusTime = statusTime;
        this.manualLocation = manualLocation;
        this.userId = userId;
        this.id = id;
        this.isPluggedIn = isPluggedIn;
        this.userLocation = userLocation;
    }

    // Getters and Setters
    public long getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(long statusTime) {
        this.statusTime = statusTime;
    }

    public boolean isManualLocation() {
        return manualLocation;
    }

    public void setManualLocation(boolean manualLocation) {
        this.manualLocation = manualLocation;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPluggedIn() {
        return isPluggedIn;
    }

    public void setPluggedIn(boolean isPluggedIn) {
        this.isPluggedIn = isPluggedIn;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }
}
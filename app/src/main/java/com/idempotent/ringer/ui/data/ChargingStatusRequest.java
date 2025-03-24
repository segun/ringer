package com.idempotent.ringer.ui.data;

public class ChargingStatusRequest {
    private String userId;
    private boolean isPluggedIn;
    private String userLocation;
    private boolean manualLocation;

    public ChargingStatusRequest(String userId, boolean isPluggedIn, String userLocation, boolean manualLocation) {
        this.userId = userId;
        this.isPluggedIn = isPluggedIn;
        this.userLocation = userLocation;
        this.manualLocation = manualLocation;
    }

    @Override
    public String toString() {
        return "ChargingStatusRequest{" +
                "userId='" + userId + '\'' +
                ", isPluggedIn=" + isPluggedIn +
                ", userLocation='" + userLocation + '\'' +
                ", manualLocation=" + manualLocation +
                '}';
    }
}

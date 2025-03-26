package com.idempotent.ringer.ui.data;
public class GetChargingStatusRequest {
    private String userId;
    private String location;
    private Long startDate;  // Using Long instead of long to allow null
    private Long endDate;    // Using Long instead of long to allow null

    // Default constructor
    public GetChargingStatusRequest() {
    }

    // Parameterized constructor
    public GetChargingStatusRequest(String userId, String location, Long startDate, Long endDate) {
        this.userId = userId;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }
}
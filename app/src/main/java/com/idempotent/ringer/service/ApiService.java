package com.idempotent.ringer.service;

import com.idempotent.ringer.ui.data.GetChargingStatusRequest;
import com.idempotent.ringer.ui.data.UpdateChargingStatusRequest;
import com.idempotent.ringer.ui.data.ChargingStatusResponse;
import com.idempotent.ringer.ui.data.LoginRequest;
import com.idempotent.ringer.ui.data.RegisterRequest;
import com.idempotent.ringer.ui.data.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("user/login")
    Call<UserResponse> login(@Body LoginRequest loginRequest);

    @POST("user/register")
    Call<UserResponse> register(@Body RegisterRequest registerRequest);

    @POST("/charging/update-status")
    Call<ChargingStatusResponse> sendChargingStatus(@Body UpdateChargingStatusRequest request);

    @POST("/charging/get-status")
    Call<List<ChargingStatusResponse>> getChargingStatus(@Body GetChargingStatusRequest request);
}
package com.idempotent.ringer.service;

import com.idempotent.ringer.ui.data.LoginRequest;
import com.idempotent.ringer.ui.data.RegisterRequest;
import com.idempotent.ringer.ui.data.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("user/login")
    Call<UserResponse> login(@Body LoginRequest loginRequest);

    @POST("user/register")
    Call<UserResponse> register(@Body RegisterRequest registerRequest);
}
package com.idempotent.ringer.utils;

import android.util.Log;

import com.idempotent.ringer.service.ApiService;
import com.idempotent.ringer.service.RetrofitClient;
import com.idempotent.ringer.ui.data.ChargingStatusRequest;
import com.idempotent.ringer.ui.data.ChargingStatusResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChargingStatusHelper {
    public static void sendChargingStatusToServer(String userId, boolean isPluggedIn, String userLocation, boolean manualLocation) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        ChargingStatusRequest request = new ChargingStatusRequest(userId, isPluggedIn, userLocation, manualLocation);

        // print request
        Log.d("olu-ringer", "Sending charging status request: " + request);

        Call<ChargingStatusResponse> call = apiService.sendChargingStatus(request);
        call.enqueue(new Callback<ChargingStatusResponse>() {
            @Override
            public void onResponse(Call<ChargingStatusResponse> call, Response<ChargingStatusResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("olu-ringer", "Charging status updated successfully.");
                } else {
                    Log.e("olu-ringer", "Failed to update charging status.");
                }
            }

            @Override
            public void onFailure(Call<ChargingStatusResponse> call, Throwable t) {
                Log.e("olu-ringer", "Error sending charging status: " + t.getMessage());
            }
        });
    }
}

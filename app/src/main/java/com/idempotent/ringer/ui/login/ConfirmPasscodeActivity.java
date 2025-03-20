package com.idempotent.ringer.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.idempotent.ringer.MainActivity;
import com.idempotent.ringer.R;
import com.idempotent.ringer.service.ApiService;
import com.idempotent.ringer.service.RetrofitClient;
import com.idempotent.ringer.ui.data.RegisterRequest;
import com.idempotent.ringer.ui.data.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmPasscodeActivity extends AppCompatActivity {

    private TextView tvTitle, tvPasscode;
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, btnDelete, btnSubmit;
    private StringBuilder passcodeBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_passcode);

        tvTitle = findViewById(R.id.tvTitle);
        tvPasscode = findViewById(R.id.tvPasscode);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        btn0 = findViewById(R.id.btn0);
        btnDelete = findViewById(R.id.btnDelete);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Set title
        tvTitle.setText("Confirm Passcode");

        // Set click listeners for numeric buttons
        View.OnClickListener numericButtonClickListener = v -> {
            if (passcodeBuilder.length() < 6) {
                Button button = (Button) v;
                passcodeBuilder.append(button.getText().toString());
                updatePasscodeDisplay();
            }
        };

        btn1.setOnClickListener(numericButtonClickListener);
        btn2.setOnClickListener(numericButtonClickListener);
        btn3.setOnClickListener(numericButtonClickListener);
        btn4.setOnClickListener(numericButtonClickListener);
        btn5.setOnClickListener(numericButtonClickListener);
        btn6.setOnClickListener(numericButtonClickListener);
        btn7.setOnClickListener(numericButtonClickListener);
        btn8.setOnClickListener(numericButtonClickListener);
        btn9.setOnClickListener(numericButtonClickListener);
        btn0.setOnClickListener(numericButtonClickListener);

        // Delete button
        btnDelete.setOnClickListener(v -> {
            if (passcodeBuilder.length() > 0) {
                passcodeBuilder.deleteCharAt(passcodeBuilder.length() - 1);
                updatePasscodeDisplay();
            }
        });

        // Submit button
        btnSubmit.setOnClickListener(v -> {
            if (passcodeBuilder.length() == 6) {
                String confirmPasscode = passcodeBuilder.toString();
                String originalPasscode = getIntent().getStringExtra("passcode");
                String emailOrPhone = getIntent().getStringExtra("emailOrPhone");
                String userLocation = getIntent().getStringExtra("userLocation");
                boolean manualLocation = getIntent().getBooleanExtra("manualLocation", false);

                if (confirmPasscode.equals(originalPasscode)) {
                    registerUser(emailOrPhone, confirmPasscode, userLocation, manualLocation);
                } else {
                    Toast.makeText(this, "Passcodes do not match", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a 6-digit passcode", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePasscodeDisplay() {
        StringBuilder displayBuilder = new StringBuilder();
        for (int i = 0; i < passcodeBuilder.length(); i++) {
            displayBuilder.append("*");
        }
        for (int i = passcodeBuilder.length(); i < 6; i++) {
            displayBuilder.append("-");
        }
        tvPasscode.setText(displayBuilder.toString());
    }

    private void registerUser(String emailOrPhone, String passcode, String userLocation, boolean manualLocation) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        Call<UserResponse> call = apiService.register(new RegisterRequest(emailOrPhone, passcode, userLocation, manualLocation));
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressBar.setVisibility(View.GONE);
                Object responseBody = response.body();
                boolean isSuccess = response.isSuccessful();
                Log.d("ringer", "Response: " + responseBody + ", isSuccessful: " + isSuccess);
                if (isSuccess && responseBody != null) {
                    // Save registration status in SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isRegistered", true);
                    editor.apply(); // Apply changes asynchronously

                    Toast.makeText(ConfirmPasscodeActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ConfirmPasscodeActivity.this, MainActivity.class));
                    finish();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ConfirmPasscodeActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(ConfirmPasscodeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
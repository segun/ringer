package com.idempotent.ringer.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.idempotent.ringer.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmailOrPhone, etLocation;
    private TextView tvLoginOrRegister;
    private Button btnNext;
    private FusedLocationProviderClient fusedLocationClient;
    private String userLocation = "";
    private boolean manualLocation = false;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IS_REGISTERED = "isRegistered";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmailOrPhone = findViewById(R.id.etEmailOrPhone);
        etLocation = findViewById(R.id.etLocation);
        tvLoginOrRegister = findViewById(R.id.loginOrRegister);
        btnNext = findViewById(R.id.btnNext);

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean isRegistered = sharedPreferences.getBoolean("isRegistered", false);

        tvLoginOrRegister.setText(isRegistered ? "Login" : "Register");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnNext.setOnClickListener(v -> {
            String emailOrPhone = etEmailOrPhone.getText().toString();
            if (emailOrPhone.isEmpty()) {
                Toast.makeText(this, "Please enter email or phone number", Toast.LENGTH_SHORT).show();
            } else {
                getLocation(() -> {
                    // If manual location entry is required, use that value
                    if (manualLocation) {
                        userLocation = etLocation.getText().toString();
                    }
                    Intent intent = new Intent(LoginActivity.this, PasscodeActivity.class);
                    intent.putExtra("emailOrPhone", emailOrPhone);
                    intent.putExtra("userLocation", userLocation);
                    intent.putExtra("manualLocation", manualLocation);
                    startActivity(intent);
                });
            }
        });

    }

    private void getLocation(Runnable onLocationFetched) {
        if (checkLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            userLocation = location.getLatitude() + "," + location.getLongitude();
                            manualLocation = false;
                        } else {
                            etLocation.setVisibility(View.VISIBLE);
                            manualLocation = true;
                        }
                        onLocationFetched.run();
                    });
        }
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation(() -> {}); // Re-attempt fetching location
            } else {
                etLocation.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Location permission denied. Please enter manually.", Toast.LENGTH_SHORT).show();
                manualLocation = true;
            }
        }
    }
}